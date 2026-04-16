package com.AgsCh.task_scheduler.port.external;

import java.util.List;
import com.AgsCh.task_scheduler.dto.external.ExternalPersonDTO;

public interface ExternalPersonSearchPort {

    List<ExternalPersonDTO> searchByName(String name);
}