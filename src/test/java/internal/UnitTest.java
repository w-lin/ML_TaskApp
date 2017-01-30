package internal;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import entity.Task;
import lambda.TaskApp;

import java.io.IOException;

public class UnitTest {

    static void dynamoDbConnectionTest()
    {
        TaskApp taskApp = getTaskApp();
        System.out.println(taskApp.listTask());
    }

    static void testUpdateItem()
    {
        Task task = new Task();
        task.setUser("wenghui.lin@gmail.com");
        task.setPriority(9);
        task.setDescription("Update Update");
        task.setCompleted("2016-07-06T12:22:46-04:00");
        task.setObjectId("22530bb7dc734374a5d339f349ec3c63");
        getTaskApp().updateTask(task);
    }

    static TaskApp getTaskApp()
    {
        AmazonDynamoDBClient dynamoDBClient;
        AWSCredentials credentials = null;
        try {
            credentials = new PropertiesCredentials(
                    TaskApp.class.getResourceAsStream("/AwsCredentials.properties"));
            dynamoDBClient = new AmazonDynamoDBClient(credentials);
        } catch (IOException e) {
            e.printStackTrace();
            dynamoDBClient = new AmazonDynamoDBClient(); //use Access Key ID and Secret Access Key from system environment
        }
        TaskApp taskApp = new TaskApp(dynamoDBClient);
        return taskApp;
    }

    public static void main(String [] s)
    {
        dynamoDbConnectionTest();
        testUpdateItem();
    }
}
