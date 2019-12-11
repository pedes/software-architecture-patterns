package org.acme.faulttolerance;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

/**
 * A JAX-RS resource that provides information about kinds of coffees we have on store and numbers of packages
 * available.
 *
 * Demonstrates {@link Retry}, {@link CircuitBreaker}, {@link Timeout} and {@link Fallback} policies.
 */
@Path("/coffee")
@Produces(MediaType.APPLICATION_JSON)
public class CoffeeResource {

    private static final Logger LOGGER = Logger.getLogger(CoffeeResource.class);

    @Inject
    CoffeeRepositoryService coffeeRepository;

    private AtomicLong counter = new AtomicLong(0);

    private Float failRatio = 0.5f;

    /**
     * Provides list of all our coffees.
     *
     * This method fails about 50% of time. However, in case of a failure, the method is automatically re-invoked again
     * (up to 4 times), thanks to the {@link Retry} annotation. That means that a user is rarely exposed to a
     * failure, since the probability of a failure occurring 4 times in row is fairly low.
     */
    @GET
    @Retry(maxRetries = 4, retryOn = RuntimeException.class)
    public List<Coffee> coffees() {
        final Long invocationNumber = counter.getAndIncrement();

        //HERE WE GENERATE SOME RANDOM ERRORS
        maybeFail(String.format("CoffeeResource#coffees() invocation #%d failed", invocationNumber));

        LOGGER.infof("CoffeeResource#coffees() invocation #%d returning successfully", invocationNumber);
        return coffeeRepository.getAllCoffees();
    }

    /**
     * Provides information about a coffee with given id.
     *
     * Same as the {@link #coffees()} method, this method fails about 50% of time. However, because this method doesn't
     * have any {@link Retry} policy, a user is exposed to the failures every time they happen.
     */
    @Path("/{id}")
    @GET
    public Response coffeeDetail(@PathParam int id) {
        final Long invocationNumber = counter.getAndIncrement();

        //HERE WE GENERATE SOME RANDOM ERRORS
        maybeFail(String.format("CoffeeResource#coffees() invocation #%d failed", invocationNumber));

        LOGGER.infof("CoffeeResource#coffees() invocation #%d returning successfully", invocationNumber);
        Coffee coffee = coffeeRepository.getCoffeeById(id);

        // if coffee with given id not found, return 404
        if (coffee == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(coffee).build();
    }

    /**
     * Returns how many packages of given coffee we have on store.
     *
     * In this case, the failures are introduced inside the CDI bean this method calls. The bean will repeat two
     * successful and two failed invocations. It also defines a {@link CircuitBreaker} policy, which will open a circuit
     * breaker after two failed invocations, and then stay open for 5 seconds.
     */
    @Path("/{id}/availability")
    @GET
    public Response availability(@PathParam int id) {
        final Long invocationNumber = counter.getAndIncrement();

        Coffee coffee = coffeeRepository.getCoffeeById(id);

        // check that coffee with given id exists, return 404 if not
        if (coffee == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            //getAvailability has the circuit breaker annotation
            Integer availability = coffeeRepository.getAvailability(coffee);
            LOGGER.infof("CoffeeResource#availability() invocation #%d returning successfully", invocationNumber);
            return Response.ok(availability).build();
        } catch (RuntimeException e) {
            String message = e.getClass().getSimpleName() + ": " + e.getMessage();
            LOGGER.errorf("CoffeeResource#availability() invocation #%d failed: %s", invocationNumber, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(message)
                    .build();
        }
    }

    /**
     * Has a timeout enabled, so if any of the internal calls takes more than 250ms to respond,
     * the timeout will be triggered. Having the fallback, avoids the user getting a timeout error, instead it will
     * get the fallback response.
     */
    @GET
    @Path("/{id}/recommendations")
    @Timeout(250)
    @Fallback(fallbackMethod = "fallbackRecommendations")
    public List<Coffee> recommendations(@PathParam int id) {
        long started = System.currentTimeMillis();
        final long invocationNumber = counter.getAndIncrement();

        try {
            randomDelay();
            LOGGER.infof("CoffeeResource#recommendations() invocation #%d returning successfully", invocationNumber);
            return coffeeRepository.getRecommendations(id);
        } catch (InterruptedException e) {
            LOGGER.errorf("CoffeeResource#recommendations() invocation #%d timed out after %d ms",
                    invocationNumber, System.currentTimeMillis() - started);
            return null;
        }
    }

    /**
     * A fallback method for recommendations.
     */
    public List<Coffee> fallbackRecommendations(int id) {
        LOGGER.info("Falling back to RecommendationResource#fallbackRecommendations()");
        // safe bet, return something that everybody likes Colombian Coffee
        return Collections.singletonList(coffeeRepository.getCoffeeById(1));
    }


    private void maybeFail(String failureLogMessage) {
        // introduce some artificial failures
        if (new Random().nextFloat() < failRatio) {
            LOGGER.error(failureLogMessage);
            throw new RuntimeException("Resource failure.");
        }
    }

    private void randomDelay() throws InterruptedException {
        // introduce some artificial delay
        Thread.sleep(new Random().nextInt(500));
    }

    void setFailRatio(Float failRatio) {
        this.failRatio = failRatio;
    }

    void resetCounter() {
        this.counter.set(0);
    }

    Long getCounter() {
        return counter.get();
    }
}