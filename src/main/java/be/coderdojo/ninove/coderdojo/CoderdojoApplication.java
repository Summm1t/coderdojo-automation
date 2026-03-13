package be.coderdojo.ninove.coderdojo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoderdojoApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(CoderdojoApplication.class);
    app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
    app.setLogStartupInfo(false);
    app.run(args);
  }

}
