Val-less
===

Val-less is the support library for programming Kotlin with val less style.

What's the val less style?
===

Writing a member function without val. In val less style, the member function
starts with equal(`=`), and does not start with brackets(`{`). In brief all member
functions is written as [Single-Expression](https://kotlinlang.org/docs/reference/functions.html#single-expression-functions)
 function.

Example
===

The example code of spring-boot.

```kotlin
  @Autowired lateinit var orderRepository: OrderRepository
  @Autowired lateinit var orderItemsRepository: OrderRepository
  @Autowired lateinit var warehouseRepository: WarehouseRepository

  @Bean
  open fun findWarehouse(orderId: Int): ResponseEntity<List<Warehouse>> {
    orderRepository.findById(orderId) ?: throw OrderNotFoundException(orderId)
    val items = orderItemsRepository.findItemsByOrderId(orderId)
    val list = warehouseRepository.findItemIn(items)
    return ResponseEntity(list)
  }
```

With val-less style, the code becomes like this.

```kotlin
  @Autowired lateinit var orderRepository: OrderRepository
  @Autowired lateinit var orderItemsRepository: OrderRepository
  @Autowired lateinit var warehouseRepository: WarehouseRepository

  @Bean
  open fun findWarehouse(orderId: Int): ResponseEntity<List<Warehouse>> =
    orderRepository.findById(orderId)
        .make { orderItemsRepository.findItemsByOrderId(orderId) }
        .make { warehouseRepository.findItemIn(it) }
        .make { ResponseEntity(it) } ?: throw OrderNotFoundException(orderId)
```

What's Val-less support?
===

For val less style, there are some lacking features in Kotlin.
For example...

* Monadic calculation function for nullable type.
* Function composition
* Returning `Unit` function for avoiding compile error.
