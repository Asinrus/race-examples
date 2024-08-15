## Race library examples


## Summary of Transaction Isolation Level Tests

This project includes two test cases that highlight the differences in handling the **Repeatable Read** isolation level between MySQL and PostgreSQL.

### MySQL: Concurrent Operations Succeed Without Errors

```java
@Container
@ServiceConnection
static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:oraclelinux8");

@Test
void testWithNaming() {
    race(Map.of(
            "Mike", () -> customerService.changeName(1L, "Mike"),
            "Derek", () -> customerService.changeName(1L, "Derek")))
        .withAssertion(executionResult -> {
            var derekResult = executionResult.get("Derek");
            var mikeResult = executionResult.get("Mike");
            assertTrue(!derekResult.isHasError() && !mikeResult.isHasError());
        })
        .go();
}
```

### PostgreSQL: One Operation Fails Due to Conflict

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");

@Test
void testWithNaming() {
    race(Map.of(
            "Mike", () -> customerService.changeName(1L, "Mike"),
            "Derek", () -> customerService.changeName(1L, "Derek")))
        .withAssertion(executionResult -> {
            var derekResult = executionResult.get("Derek");
            var mikeResult = executionResult.get("Mike");
            var oneContainsError = mikeResult.isHasError() ^ derekResult.isHasError();
            assertTrue(oneContainsError);
        })
        .go();
}
```