package configuration;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VinProcessor implements ItemProcessor<Vin, Vin>{

    @Autowired
    CSVService csvService;

    @Override
    public Vin process(final Vin vin) {
        csvService.getVinAndModel(vin);
        return vin;
    }
}
