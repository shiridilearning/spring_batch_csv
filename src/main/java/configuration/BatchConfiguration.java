package configuration;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("file:${input.files.location}")
    private Resource[] inputFolder;

    @Value("${output.folder}")
    private String outputFolder;

    @Value("${chunk.size}")
    private String chunkSize;

    @Bean
    public Job importVinJob(NotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importVinJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(ItemWriter<Vin> writer) {
        return stepBuilderFactory.get("step1")
                .<Vin, Vin> chunk(Integer.parseInt(chunkSize))
                .reader(multiResourceItemReader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Vin> multiResourceItemReader()
    {
        System.out.println("inside multi reader");
        MultiResourceItemReader<Vin> resourceItemReader = new MultiResourceItemReader<>();
        resourceItemReader.setResources(inputFolder);
        resourceItemReader.setDelegate(reader());
        return resourceItemReader;
    }

    @Bean
    public FlatFileItemReader<Vin> reader() {
        System.out.println("inside reader");
        FlatFileItemReader<Vin> reader = new FlatFileItemReader<>();
        reader.setLineMapper(new DefaultLineMapper<Vin>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("vinno");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Vin>() {{
                setTargetType(Vin.class);
            }});

        }});

        return reader;
    }

    @Bean
    public VinProcessor processor() {
        System.out.println("inside processor");
        return new VinProcessor();
    }

    @Bean
    public ItemWriter<Vin> writer() throws IOException {
        System.out.println("inside writer");
        //clean folder first
        FileUtils.cleanDirectory(new File(outputFolder));
        //Create writer instance
        FlatFileItemWriter<Vin> writer = new FlatFileItemWriter<>();

        writer.setName("chunkFileItemWriter");

        //for headers in each excel file
        writer.setHeaderCallback(writer1 -> writer1.write("vin,model"));

        //All job repetitions should "append" to same output file
        writer.setAppendAllowed(false);

        //Name field values sequence based on object properties
        writer.setLineAggregator(new DelimitedLineAggregator<Vin>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<Vin>() {
                    {
                        setNames(new String[] { "vinno" , "model"});
                    }
                });
            }
        });
        AtomicInteger i= new AtomicInteger(1);
        System.out.println("got items");
        return items -> {
            Map<String, List<Vin>> segregated = new LinkedHashMap<>();
            segregated = items.stream().collect(Collectors.groupingBy(Vin::getInputSrcFileName,LinkedHashMap::new,Collectors.toList()));
            Set<Map.Entry<String, List<Vin>>> entrySet = segregated.entrySet();
            for (Map.Entry<String, List<Vin>> currentEntry :
                    entrySet) {
                writer.setResource(new FileSystemResource(outputFolder + currentEntry.getKey()+"/"+i.getAndIncrement()+".csv"));
                writer.open(new ExecutionContext());
                writer.write(currentEntry.getValue());
                writer.close();
            }
        };
    }

}
