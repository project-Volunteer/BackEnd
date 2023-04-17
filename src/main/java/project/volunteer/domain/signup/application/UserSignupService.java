package project.volunteer.domain.signup.application;

import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;

public interface UserSignupService {

	void addUser(UserSignupRequest userSignup);

}
