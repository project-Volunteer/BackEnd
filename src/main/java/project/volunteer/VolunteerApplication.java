package project.volunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //Spring Scheduler 사용
@EnableJpaAuditing	//Spring Data Jpa - Auditing 사용
@SpringBootApplication
public class VolunteerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolunteerApplication.class, args);
	}



}
