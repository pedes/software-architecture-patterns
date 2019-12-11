Long Running Activity Demo...

- Demonstrates microprofile-lra standard functionality: https://github.com/eclipse/microprofile-lra
- Using Naryana implementation: https://github.com/jbosstm/narayana/tree/master/rts/lra
- Running on Helidon MP. (*mods were made to the current Narayana implementation for this)
- Demo simply uses System.out messages for verifications.

3 microservices:

LRA Coordinator:
- *Note narayana pom.xml dependencies and need to repackage lra-coordinator.war as jar for Helidon execution.
- Narayana coordinator running on Helidon

Order Service:
- *Note narayana pom.xml dependencies and io.narayana.lra.filter.FilterRegistration DynamicFeature added in app.
- call to placeOrder by client begins an LRA due to @LRA RequiresNew annotation and registers service with the LRA coordinator.
- calls inventory service to check inventory with/propagating LRA_HTTP_CONTEXT_HEADER with the LRA id.
- if inventory exists (the default) success is returned to order service which implicitly completes the LRA and coordinator calls the @Complete annotated method completeOrder
- if inventory does not exist (can be achieved by calling the removeInventory endpoint on inventory service) failure is returned to order service which cancels the LRA and coordinator calls the @Compensate annotated method cancelOrder

Inventory Service:
- *Note narayana pom.xml dependencies and io.narayana.lra.filter.FilterRegistration DynamicFeature added in app.
- call to reserveInventoryForOrder by order service is executed within LRA started and propagated by order service due to @LRA Mandatory annotation and registers service with the LRA coordinator
- calls inventory service to check and returns the result (success or fail) to the inventory service.

To build*:
- ./build.sh

To run 
- java -jar lra-coordinator-helidon/target/lra-coordinator-helidon-0.0.1-SNAPSHOT.jar
- java -jar order/target/order-0.0.1-SNAPSHOT.jar
- java -jar inventory/target/inventory-0.0.1-SNAPSHOT.jar

For success/complete case ...
- curl http://localhost:8091/inventory/addInventory
- curl http://localhost:8090/order/placeOrder 

You should see the following output in the Order Service...
```
OrderResource.placeOrder in LRA due to LRA.Type.REQUIRES_NEW lraId:[...]

OrderResource.placeOrder response from inventory:inventorysuccess

OrderResource.completeOrder
```

And the following output in the Inventory Service...
```
InventoryResource.addInventory

InventoryResource.placeOrder in LRA due to LRA.Type.MANDATORY lraID:[...]

InventoryResource.completeOrder prepare item for shipping lraId:[...]
```

For failure/compensate case...
- curl http://localhost:8091/inventory/removeInventory
- curl http://localhost:8090/order/placeOrder 

You should see the following output in the Order service...
```
OrderResource.placeOrder in LRA due to LRA.Type.REQUIRES_NEW lraId:[...]

OrderResource.placeOrder response from inventory:inventoryfailure

OrderResource.cancelOrder
```

And the following output in the Inventory Service...
```
InventoryResource.removeInventory

InventoryResource.placeOrder in LRA due to LRA.Type.MANDATORY lraID:[...]

InventoryResource.cancelOrder put inventory back lraId:[...]
```