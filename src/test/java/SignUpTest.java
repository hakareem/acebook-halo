import com.github.javafaker.Faker;
import com.makersacademy.acebook.Application;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class SignUpTest {

    WebDriver driver;
    Faker faker;

    @Before
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        driver = new ChromeDriver();
        faker = new Faker();
    }

    @After
    public void tearDown() {
        driver.close();
    }

    @Test
    public void successfulSignUpRedirectsToSignIn() {
        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys(faker.name().firstName());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("signup")).click();
        String title = driver.getTitle();
        Assert.assertEquals("Login", title);
    }

    @Test
    public void unsuccessfulSignUpTestBlankUsername() {
        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys("");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("signup")).click();
        String url = driver.getCurrentUrl();
        Assert.assertEquals("http://localhost:8080/users/new", url);
    }

    @Test
    public void unsuccessfulSignUpTestBlankPassword() {
        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys(faker.name().firstName());
        driver.findElement(By.id("password")).sendKeys("");
        driver.findElement(By.id("signup")).click();
        String url = driver.getCurrentUrl();
        Assert.assertEquals("http://localhost:8080/users/new", url);
    }

    @Test
    public void unsuccessfulSignUpTestPassCharMin() {
        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys(faker.name().firstName());
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.id("signup")).click();
        String url = driver.getCurrentUrl();
        Assert.assertEquals("http://localhost:8080/users/new", url);
    }

    @Test
    public void unsuccessfulSignUpExistUsername() {
        String name = faker.name().firstName();
        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys(name);
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("signup")).click();

        driver.get("http://localhost:8080/users/new");
        driver.findElement(By.id("username")).sendKeys(name);
        driver.findElement(By.id("password")).sendKeys("123456789");
        driver.findElement(By.id("signup")).click();

        String url = driver.getCurrentUrl();
        Assert.assertEquals("http://localhost:8080/users/new?retry", url);
    }
}
