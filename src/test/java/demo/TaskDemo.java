package demo;

import com.sun.jersey.api.client.*;

/*
{
  "user" : "wenghui.lin@gmail.com",
  "priority": 3,
  "description": "Task1",
  "completed": "2015-06-08T15:57:00-04:00"
 }
 */
public class TaskDemo {

    static String API_URL = "https://x29omonrzg.execute-api.us-east-1.amazonaws.com/development/task";

    static WebResource webResource;

    public static void main(String s [])
    {
        Client client = Client.create();
        webResource = client.resource(API_URL);
        listTask();
        addTask();
        updateTask();
        delteTask();
        listTask();
    }


    public static void listTask()
    {
        System.out.println(webResource.get(String.class));
    }

    public static void addTask()
    {
        String input = "{\"priority\": 6, \"description\": \"API Upload 1\", \"completed\": \"2017-1-30T15:57:00-04:00\", \"user\": \"wenghui.lin@gmail.com\"}";
        System.out.println(webResource.put(String.class, input));
        input = "{\"priority\": 6, \"description\": \"API Upload 2\", \"user\": \"wenghui.lin@gmail.com\"}";
        System.out.println(webResource.put(String.class, input));
        input = "{\"priority\": 6, \"description\": \"API Upload 3\"}";
        System.out.println(webResource.put(String.class, input));
    }

    public static void updateTask()
    {
        String input = "{\"priority\": 7, \"description\": \"API Upload 1 Updated\", \"completed\": \"2017-1-31T15:57:00-04:00\", \"user\": \"wenghui.lin@gmail.com\"}";
        System.out.println(webResource.path("c8caa28fa5304def8b3491ad2e3e0a94").type("application/json").post(String.class, input));
    }


    public static void delteTask()
    {
        System.out.println(webResource.path("cde7855bfa474334ba6c69dbfa37f2c6").delete(String.class));
    }



}
