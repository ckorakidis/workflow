# Introduction

## Sample POC providing the following functionality

- Create workflows with a given number of steps
- Create a workflow execution
- Increment a workflow execution state
- Return an execution state
- Run a periodic nonblocking background job every 1 minute to remove workflow executions that are finished and older than 1 minute.

### Data Model

```
workflow:
 - workflow id
 - number of steps

workflow_execution:
 - workflow id
 - workflow execution id
 - current step index
 - creation timestamp
```

### Endpoints


#### Create workflows with a given number of steps
```
url: http://localhost:8080/workflow
method: POST
example body:
{
    "id":1,
    "steps":1
}
```

#### Create executions for a given workflow (initial step index should be 0)
```
url: http://localhost:8080/execution
method: POST
example body:
{
    "id":1,
    "workflowId":1
}
```

#### Increment current step index for a given execution
```
url: http://localhost:8080/execution/1/increase
method: PUT
```

#### Querying a given execution state (whether the execution is finished or not)
```
url: http://localhost:8080/execution
method: GET
example result:
{
    "id":1,
    "workflowId":1,
    "currentStep":0,
    "timestamp":"2018-06-21 22:14:28"
}
```

# Instructions

## Prerequisites
* sbt installed and included in path
* curl is available

## Steps
1. From project root (console): sbt run
2. Call the available endpoints using curl
```
curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"id":1,"steps":1}' \
  http://localhost:8080/workflow

 curl --header "Content-Type: application/json" \
  --request POST \
  --data '{"id":1,"workflowId":1}' \
  http://localhost:8080/execution


  curl --request PUT http://localhost:8080/execution/1/increase

  curl --request GET http://localhost:8080/execution/1

```