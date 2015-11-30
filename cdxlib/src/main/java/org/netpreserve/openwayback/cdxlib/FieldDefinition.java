/*
 * Copyright 2015 IIPC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.openwayback.cdxlib;

import java.util.Objects;

/**
 *
 */
public class FieldDefinition {

    private final FieldType type;

    private final String customName;

    public FieldDefinition(FieldType type) {
        this.type = type;
        this.customName = null;
    }

    public FieldDefinition(String customName) {
        this.type = FieldType.custom;
        this.customName = customName;
    }

    public String getName() {
        if (type == FieldType.custom) {
            return customName;
        } else {
            return type.getName();
        }
    }

    public FieldType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CdxField{" + "type=" + type.name() + ", name=" + getName() + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.type);
        hash = 23 * hash + Objects.hashCode(this.customName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldDefinition other = (FieldDefinition) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.customName, other.customName)) {
            return false;
        }
        return true;
    }

}
