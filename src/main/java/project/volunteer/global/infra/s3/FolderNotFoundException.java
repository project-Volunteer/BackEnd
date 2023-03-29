package project.volunteer.global.infra.s3;

public class FolderNotFoundException extends RuntimeException{

    public FolderNotFoundException() {
    }

    public FolderNotFoundException(String message) {
        super(message);
    }
}
