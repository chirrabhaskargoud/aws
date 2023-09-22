package ecs_fargate;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeNetworkInterfacesRequest;
import com.amazonaws.services.ec2.model.DescribeNetworkInterfacesResult;
import com.amazonaws.services.ec2.model.NetworkInterfaceAssociation;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.AssignPublicIp;
import com.amazonaws.services.ecs.model.AwsVpcConfiguration;
import com.amazonaws.services.ecs.model.CreateClusterRequest;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.DescribeTasksResult;
import com.amazonaws.services.ecs.model.KeyValuePair;
import com.amazonaws.services.ecs.model.LaunchType;
import com.amazonaws.services.ecs.model.NetworkConfiguration;
import com.amazonaws.services.ecs.model.RunTaskRequest;
import com.amazonaws.services.ecs.model.RunTaskResult;
import com.amazonaws.services.ecs.model.Task;

public class CreateEcsClusterAndRunTask {
	static String clusterName = "seleniumcluster";
	public static String createEcsClusterAndRunTask(String taskDefinitionArn) throws InterruptedException {
		int desiredCount = 1; // Number of tasks to run

		// Create an Amazon ECS client
		AmazonECS ecsClient = AmazonECSClientBuilder.defaultClient();

		// Create an ECS cluster
		CreateClusterRequest createClusterRequest = new CreateClusterRequest().withClusterName(clusterName);

		ecsClient.createCluster(createClusterRequest);

		Thread.sleep(20000);
		String securityGroupString = "sg-07da82d1dbb315f18";
		List<String> subNetlist = new ArrayList<String>();
		subNetlist.add("subnet-0f70d333d97fd5094");
		subNetlist.add("subnet-027e198244f2b6df7");
		subNetlist.add("subnet-055ba65e886efbf0b");

		AwsVpcConfiguration awsvpcConfiguration = new AwsVpcConfiguration().withSecurityGroups(securityGroupString)
				.withSubnets(subNetlist).withAssignPublicIp(AssignPublicIp.ENABLED);

		NetworkConfiguration networkConfiguration = new NetworkConfiguration()
				.withAwsvpcConfiguration(awsvpcConfiguration);

		// Run tasks in the cluster
		RunTaskRequest runTaskRequest = new RunTaskRequest().withCluster(clusterName)
				.withTaskDefinition(taskDefinitionArn).withLaunchType(LaunchType.FARGATE).withPlatformVersion("LATEST")
				.withNetworkConfiguration(networkConfiguration).withCount(desiredCount);

		RunTaskResult runTaskResult = ecsClient.runTask(runTaskRequest);
		Thread.sleep(30000);
		// Output task ARNs
		for (Task task : runTaskResult.getTasks()) {
			System.out.println("Task ARN: " + task.getTaskArn());
		}

		return runTaskResult.getTasks().get(0).getTaskArn();
	}
	

	public static String getPublicIPAddress(String taskArn) throws InterruptedException {
		AmazonECS ecsClient = AmazonECSClientBuilder.defaultClient();
		AmazonEC2 ec2Client = AmazonEC2ClientBuilder.defaultClient();

		DescribeTasksRequest request = new DescribeTasksRequest().withCluster(clusterName).withTasks(taskArn);
		DescribeTasksResult response = ecsClient.describeTasks(request);
		Task task = response.getTasks().get(0);
		KeyValuePair networkInterfeKeyValuePair = task.getAttachments().get(0).getDetails().get(1).withName("networkInterfaceId");

		String networkInterfaceId = networkInterfeKeyValuePair.getValue();
		DescribeNetworkInterfacesRequest describeNetworkInterfacesRequest = new DescribeNetworkInterfacesRequest()
				.withNetworkInterfaceIds(networkInterfaceId);

		DescribeNetworkInterfacesResult describeNetworkInterfacesResult = ec2Client
				.describeNetworkInterfaces(describeNetworkInterfacesRequest);
		com.amazonaws.services.ec2.model.NetworkInterface networkInterface = describeNetworkInterfacesResult
				.getNetworkInterfaces().get(0);

		// Extract the public IP address if available
		NetworkInterfaceAssociation association = networkInterface.getAssociation();
		String publicIPString = null;
		if (association != null && association.getPublicIp() != null) {
			publicIPString = association.getPublicIp();
			System.out.println("Public IP Address: " + publicIPString);
		} else {
			System.out.println("No public IP address associated with the network interface.");
		}
		Thread.sleep(30000);
		return publicIPString;
	}
}
