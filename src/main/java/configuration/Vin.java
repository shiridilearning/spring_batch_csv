package configuration;


import org.springframework.batch.item.ResourceAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Vin implements ResourceAware {

    public Resource getResource() {
        return resource;
    }

    public String getInputSrcFileName() {
        return inputSrcFileName;
    }

    public void setInputSrcFileName(String inputSrcFileName) {
        this.inputSrcFileName = inputSrcFileName;
    }

    private Resource resource;
    private String inputSrcFileName;

    public Vin() {
    }

    public String getVinno() {
        return vinno;
    }

    public void setVinno(String vinno) {
        this.vinno = vinno;
    }

    String vinno;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    String model;

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        this.inputSrcFileName = resource.getFilename();
    }
}
