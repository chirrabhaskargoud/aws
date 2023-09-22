package ecs_fargate;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SampleTest {

	public static void main(String[] args) throws MalformedURLException {
		WebDriver driver = null;
		try {
			
			String taskDefArn=TaskDefinition.createTaskDefinition();
			String taskArn=CreateEcsClusterAndRunTask.createEcsClusterAndRunTask(taskDefArn);
			String publicIPAddress=CreateEcsClusterAndRunTask.getPublicIPAddress(taskArn);
			
			String hubUrl = "http://"+publicIPAddress+":4444/wd/hub";
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(hubUrl, false);
			capabilities.setBrowserName("chrome");

			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--disable-dev-shm-usage");
			chromeOptions.addArguments("--no-sandbox");
			chromeOptions.addArguments("--headless");
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

			driver = new RemoteWebDriver(new URL(hubUrl), capabilities);
			driver.get("https://google.com/");
			System.out.println("title is " + driver.getTitle());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}
