package byzas.works.springbatchsample.repository;


import byzas.works.springbatchsample.domain.SampleTableEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SampleTableRepository extends PagingAndSortingRepository<SampleTableEntity, Long> {

    @Query("select c from SampleTableEntity c where c.id < 100")
    Page<SampleTableEntity> findRecords(Pageable pageable);

}
