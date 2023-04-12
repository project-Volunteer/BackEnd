package project.volunteer.domain.signup.application;

import project.volunteer.domain.signup.api.dto.request.UserSignupDTO;

public interface UserSignupService {

	void addUser(UserSignupDTO userSignup);

}
