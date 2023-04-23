package project.volunteer.domain.signup.application;

import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;

public interface UserSignupService {

	Long addUser(UserSignupRequest userSignup);

}
