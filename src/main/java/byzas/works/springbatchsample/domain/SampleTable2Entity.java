package byzas.works.springbatchsample.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @ToString
@Table(name = "sampletable2")
public class SampleTable2Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer x;
    private String y;
    private Date z;
    private Date t;
}
