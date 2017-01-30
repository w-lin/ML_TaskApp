AWS API Gateway: https://x29omonrzg.execute-api.us-east-1.amazonaws.com/development/task

AWS API Gateway is exported to src\main\resources\taskAppAPI-development-swagger-integrations,authorizers,documentation.json

src\test\java\demo\TaskDemo.java uses above gateway to list, add, update and delete tasks

The dependency <groupId>com.github.fge</groupId> <artifactId>json-schema-validator</artifactId> is used to validate Json entity against Json schema v4. 
However this library has a bug where non-required attribute is still subject to validation even if the Json entity does not contain the attribute.