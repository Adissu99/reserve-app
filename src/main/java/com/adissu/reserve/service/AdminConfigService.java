package com.adissu.reserve.service;

import com.adissu.reserve.entity.AdminConfig;
import com.adissu.reserve.repository.AdminConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private final AdminConfigRepository configRepository;
    private final List<AdminConfig> adminConfigList;

    // available names inside AdminConfigConstants
    @Deprecated // Using List<AdminConfig> Bean at the moment.
    public String getValueForConfig(String configName) {
        Optional<AdminConfig> configOptional = configRepository.findByName(configName);
        if( configOptional.isEmpty() ) {
            return null;
        }

        return configOptional.get().getValue();
    }

    public String modifyValueForConfig(String configName, String value) {
        Optional<AdminConfig> configOptional = configRepository.findByName(configName);
        if( configOptional.isEmpty() ) {
            return "ERROR";
        }

        AdminConfig config = configOptional.get();
        config.setValue(value);
        configRepository.save(config);

        adminConfigList.stream()
                .filter(adminConfig -> adminConfig.getName().equals(configName))
                .findFirst()
                .get().setValue(value);

        return "SUCCESS";
    }

    public String addNewConfig(String configName, String value) {
        if( configName == null || configName.equals("") ) {
            return "EMPTY.NAME";
        }
        if( value == null || value.equals("") ) {
            return "EMPTY.VALUE";
        }

        AdminConfig adminConfig = AdminConfig.builder()
                .name(configName)
                .value(value)
                .build();

        configRepository.save(adminConfig);

        adminConfigList.add(adminConfig);

        return "SUCCESS";
    }
}
