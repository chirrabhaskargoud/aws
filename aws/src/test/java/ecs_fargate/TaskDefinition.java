package ecs_fargate;

import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.*;

public class TaskDefinition {

	public static String createTaskDefinition() throws InterruptedException {
		String taskDefinitionName = "xxxxxxx";
		String containerName = "xxxxxxx";
		String image = "public.ecr.aws/g4l8l7t0/seleniumchrome";

		AmazonECS ecsClient = AmazonECSClientBuilder.defaultClient();
		PortMapping portMapping = new PortMapping().withContainerPort(4444).withHostPort(4444);
		ContainerDefinition containerDefinition = new ContainerDefinition().withName(containerName).withImage(image)
				.withPortMappings(portMapping).withMemory(3072).withCpu(1024);

		RegisterTaskDefinitionRequest taskDefinitionRequest = new RegisterTaskDefinitionRequest()
				.withFamily(taskDefinitionName).withNetworkMode(NetworkMode.Awsvpc)
				.withRequiresCompatibilities(Compatibility.FARGATE)
				.withExecutionRoleArn("arn:aws:iam::xxxxxxx:role/xxxxxxx")
				.withCpu(String.valueOf("1024")).withMemory(String.valueOf("3072"))
				.withContainerDefinitions(containerDefinition);

		// Register the task definition
		RegisterTaskDefinitionResult result = ecsClient.registerTaskDefinition(taskDefinitionRequest);

		String arnString=result.getTaskDefinition().getTaskDefinitionArn();
		// Output the ARN of the created task definition
		System.out.println("Task Definition ARN: " + arnString);
		Thread.sleep(15000);
		return arnString;
	}
	
}
