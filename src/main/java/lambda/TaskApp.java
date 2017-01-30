package lambda;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import entity.Task;
import utils.EmailUtil;
import utils.JsonSchemaUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskApp {

    AmazonDynamoDBClient dynamoDBClient;
    private DynamoDB dynamoDB ;
    private final String TASK_TABLE_NAME = "task";
    private final String TASK_SCHEMA_LOCATION = "/jsonschema/task.schema.json";
    private static final Logger LOGGER = Logger.getLogger(TaskApp.class.getName());

    public TaskApp()
    {
        //Usa IAM assume role
        dynamoDBClient = new AmazonDynamoDBClient();
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient());
    }

    public TaskApp(AmazonDynamoDBClient dynamoDBClient)
    {
        this.dynamoDBClient = dynamoDBClient;
        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    public String addTask(Task task)
    {
        try {
            JsonSchemaUtil.isValid(task, TASK_SCHEMA_LOCATION);
        }
        catch (Exception ignore)
        {
            return ignore.getMessage(); // return validation result e.g. numeric instance is greater than the required maximum (maximum: 9, found: 10)
        }
        try {
            Table table = dynamoDB.getTable(TASK_TABLE_NAME);
            String objectId = UUID.randomUUID().toString().replaceAll("-", "");
            Item item = new Item()
                    .withPrimaryKey("objectId", UUID.randomUUID().toString().replaceAll("-", ""))
                    .withString("description", task.getDescription())
                    .withInt("priority", task.getPriority());
            if(task.getUser()!=null && !task.getUser().trim().equals(""))
                item= item.withString("userEmail", task.getUser().trim());
            if(task.getCompleted()!=null && !task.getCompleted().trim().equals(""))
                item= item.withString("completed", task.getCompleted().trim());
            table.putItem(item);
            String result = "Task [objectId=" + objectId + "] has been added.";

            String emailVerificationResult = null;
            if (task.getUser() != null && !task.getUser().trim().equals(""))
                emailVerificationResult = EmailUtil.sendEmailVerificationIfNecessary(task.getUser());
            if (emailVerificationResult != null)
                result += " " + emailVerificationResult;
            return result;
        }
        catch (Exception e)
        {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return "Error in adding task.";
        }
     }

    public String updateTask(Task task)
    {
        if(task.getObjectId()==null || task.getObjectId().trim().equals(""))
            return "Task objectId is required.";
        task.setObjectId(task.getObjectId().trim());
        if(!checkTaskExist(task.getObjectId()))
            return "Task [objectId=" + task.getObjectId() + "] does not exist.";
        try {
            JsonSchemaUtil.isValid(task, TASK_SCHEMA_LOCATION);
        }
        catch (Exception ignore)
        {
            return ignore.getMessage(); // return validation result e.g. numeric instance is greater than the required maximum (maximum: 9, found: 10)
        }
        try {
            Table table = dynamoDB.getTable(TASK_TABLE_NAME);

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("objectId", task.getObjectId())
                    .withUpdateExpression("set priority = :priority, description=:description, userEmail=:userEmail, completed=:completed")
                    .withValueMap(new ValueMap()
                            .withInt(":priority", task.getPriority())
                            .withString(":description", task.getDescription())
                            .withString(":userEmail", task.getUser())
                            .withString(":completed", task.getCompleted()))
                    ;
            table.updateItem(updateItemSpec);
            String result = "task [objectId=" + task.getObjectId() + "] has been update.";

            String emailVerificationResult = null;
            if (task.getUser() != null && !task.getUser().trim().equals(""))
                emailVerificationResult = EmailUtil.sendEmailVerificationIfNecessary(task.getUser());
            if (emailVerificationResult != null)
                result += " " + emailVerificationResult;
            return result;
        }
        catch (Exception e)
        {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return "Error in updating task.";
        }
    }

    public String listTask()
    {
        ScanRequest scanRequest = new ScanRequest().withTableName(TASK_TABLE_NAME);
        ScanResult result = dynamoDBClient.scan(scanRequest);
        return result.getItems().toString();
     }

     public String deleteTask(String objectId)
     {
         if(objectId==null || objectId.trim().equals(""))
             return "Task objectId is required.";
         objectId = objectId.trim();
         Table table = dynamoDB.getTable(TASK_TABLE_NAME);

         DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                 .withPrimaryKey("objectId", objectId)
                 .withReturnValues(ReturnValue.ALL_OLD);

         DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);

         if(outcome.getItem()==null)
             return "Task [objectId=" + objectId + "] does not exist.";
         return "Task " + outcome.getItem().toJSONPretty() +" deleted.";
     }

     public void emailUncompletedTasks()
     {
         Map<String, Condition>  scanFilter = new HashMap<String, Condition>();
         scanFilter.put("completed", new Condition().withComparisonOperator("NULL"));
         ScanRequest scanRequest = new ScanRequest()
                 .withTableName(TASK_TABLE_NAME).withScanFilter(scanFilter);
         ScanResult result = dynamoDBClient.scan(scanRequest);
         for (Map<String, AttributeValue> item : result.getItems()) {
             if(item.get("userEmail")!=null)
             {
                 String email = item.get("userEmail").getS();
                 if(email!=null)
                     EmailUtil.sendEmail(email, "Uncompleted Task" ,"You have an uncompleted task [objectId=" + item.get("objectId").getS()+"].");
             }
         }
     }

    private boolean checkTaskExist(String primaryKeyValue)
    {
        Table table = dynamoDB.getTable(TASK_TABLE_NAME);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("objectId = :id")
                .withValueMap(new ValueMap().withString(":id", primaryKeyValue));
        ItemCollection<QueryOutcome> items = table.query(spec);
        Iterator<Item> iterator = items.iterator();
        return iterator.hasNext();
    }
}
