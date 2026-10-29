# Universal ORM Framework

A storage-independent relational query framework for Java that provides a unified API for querying and managing data across multiple persistence formats including SQL databases, XML files, JSON files, CSV files, and in-memory collections.

---

## Overview

Traditional ORM frameworks are tightly coupled to relational databases. This framework takes a different approach by introducing a unified query abstraction capable of operating on multiple storage backends while exposing a consistent programming model.

The goal is simple:

**Write a query once and execute it against different storage technologies without changing business logic.**

---

## Key Features

### Multi-Storage Support

Supported storage backends:

* SQL Databases
* XML Files
* JSON Files
* CSV Files
* In-Memory Collections

All backends share the same query model and execution pipeline.

---

### Metadata-Driven Architecture

The framework uses a cached `Metamodel` abstraction that:

* Maps entity fields
* Resolves column metadata
* Minimizes reflection overhead
* Provides storage-independent field access

---

### EntityManager Abstraction

Each entity is managed by its own EntityManager.

Example:

```java
BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
EntityManager<Person> entityManagerPerson = beanManager.getInstance(Person.class,RelationJoinManager.class); // for sql
// likewise have classes like CSVEntityManager, XMLEntityManager, JsonEntityManager
```

Responsibilities:

* Loading data
* Persisting data
* Object mapping
* Query execution
* Metrics generation

---

### Dynamic Query API

Example:

```java
Condition<Person> condition =
    Condition.greaterThan(
        Person.class,
        "age",
        20
    );
```

Composite conditions:

```java
Condition.and(
    Condition.greaterThan(Person.class, "age", 20),
    Condition.lessThan(Person.class, "salary", 100000)
);
```

---

### Supported Operators

| Operator    | Description            |
| ----------- | ---------------------- |
| EQ          | Equals                 |
| NE          | Not Equals             |
| GT          | Greater Than           |
| GTE         | Greater Than or Equals |
| LT          | Less Than              |
| LTE         | Less Than or Equals    |
| IN          | Collection Membership  |
| LIKE        | Pattern Matching       |
| BETWEEN     | Range Search           |
| IS_NULL     | Null Check             |
| IS_NOT_NULL | Not Null Check         |
| AND         | Composite Condition    |
| OR          | Composite Condition    |

---

## Join Support

The framework supports relational joins across storage systems.

Example:

```java
Join<Person, Vendor>
```

Supported Join Types:

* INNER JOIN
* LEFT JOIN

---

### Cross Entity Filtering

Example:

```java
Condition.and(
    Condition.greaterThan(
        Person.class,
        "age",
        20
    ),
    Condition.lessThanOrEqualsTo(
        Vendor.class,
        "price",
        200
    )
);
```

The framework evaluates conditions using a relational execution context represented internally through joined rows.

---

## Storage Independent Execution

The same query can be executed against:

```text
SQL Database
JSON File
XML File
CSV File
In-Memory Collection
```

without changing the query definition.

---

## In-Memory Query Engine

The framework contains an in-memory execution engine capable of:

* Condition evaluation
* Join processing
* Relational row construction
* Cross-entity filtering
* Dynamic operator execution

This enables relational querying even when data is stored outside traditional databases.

---

## SQL Query Generation

The SQL Executor translates condition trees into SQL statements.

Example:

```java
Condition.and(
    Condition.greaterThan(
        Person.class,
        "age",
        20
    ),
    Condition.lessThanOrEqualsTo(
        Vendor.class,
        "price",
        200
    )
);
```

Generated SQL:

```sql
WHERE person.age > ?
AND vendor.price <= ?
```

Parameter binding is handled separately from SQL generation.

---

## Architecture

```text
Query
 │
 ├── Condition Tree
 │
 ├── Join Graph
 │
 ▼
Executor
 │
 ├── SQLExecutor
 ├── InMemoryExecutor
 ▼
EntityManager
 │
 ▼
Storage Layer
```

---

## Example

```java
Query<Person> query =
    Query.from(Person.class)
         .where(
             Condition.greaterThan(
                 Person.class,
                 "age",
                 20
             )
         );

List<Person> result =
    entityManager.execute(query);
```

---

## Current Capabilities

### Query Features

* Dynamic Conditions
* Composite Conditions
* Join Support
* Null Handling
* Type Aware Conditions
* Storage Independent Queries

### Execution Features

* SQL Execution
* In-Memory Execution
* Metadata Caching
* Reflection Optimization
* Relational Row Processing

---

# Roadmap

The following features are planned for upcoming releases.

---

## MetricsManager Integration

Aggregation support will be integrated directly into the query execution pipeline.

Example:

```java
MetricsManager<Integer> metrics =
    entityManager.getMetrics(
        Integer.class,
        "age"
    );

metrics.max();
metrics.min();
metrics.avg();

metrics.aggregateFunction(
    Integer::sum
);
```

Benefits:

* Storage-independent aggregation
* Reusable aggregation engine
* Custom aggregation support

---

## Query-Based Aggregations

Example:

```java
Query<Person> query =
    Query.from(Person.class)
         .where(
             Condition.greaterThan(
                 Person.class,
                 "age",
                 20
             )
         );

query.max(
    Person.class,
    "age"
);
```

Aggregation will execute against filtered query results.

---

## Group By Support

Planned syntax:

```java
query.groupBy(
    Person.class,
    "department"
);
```

Potential aggregations:

```java
count()
sum()
avg()
min()
max()
```

Example:

```text
Department | Avg Salary
-----------|-----------
IT         | 85000
HR         | 60000
Finance    | 92000
```

---

## Ordering Support

```java
query.orderBy(
    Person.class,
    "name"
);

query.orderByDesc(
    Person.class,
    "salary"
);
```

---

## Pagination Support

```java
query.limit(100)
     .offset(200);
```

---

## Query Optimization

Future work includes:

* Predicate optimization
* Join optimization
* Query planning
* Metadata pre-compilation
* Advanced caching

---

## Long-Term Vision

Build a unified relational query engine capable of executing the same query model across heterogeneous storage systems while maintaining a consistent developer experience.

The framework aims to bridge the gap between traditional ORM systems and modern storage-independent application architectures.

---

## Contributing

Contributions, ideas, bug reports, and feature requests are welcome.

---

## License

License information will be added before the first public release.
