package project.volunteer.domain.recruitment.performance;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RecruitmentDetailPerformanceTest {
    @Autowired MockMvc mockMvc;

    final String FIND_URL = "/recruitment/";
//    @Test
//    public void recruitment_details_refer_performance_test() throws Exception {
//
//        mockMvc.perform(get(FIND_URL + 1))
//                .andExpect(status().isOk());
//    }
}
