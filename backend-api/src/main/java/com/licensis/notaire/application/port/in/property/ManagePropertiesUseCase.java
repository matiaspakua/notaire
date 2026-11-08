package com.licensis.notaire.application.port.in.property;

import com.licensis.notaire.domain.property.Property;
import com.licensis.notaire.domain.property.PropertyDetails;

public interface ManagePropertiesUseCase {

    Property create(PropertyDetails details);

    void update(int id, PropertyDetails details);

    void delete(int id);
}
