package thesis.rommler.federation_controller.api.answerClasses;

public class FileInformation {
    public String filename;
    public String filetype;
    public String filesize;

    public FileInformation(String filename, String filetype, String filesize){
        this.filename = filename;
        this.filetype = filetype;
        this.filesize = filesize;
    }

}
