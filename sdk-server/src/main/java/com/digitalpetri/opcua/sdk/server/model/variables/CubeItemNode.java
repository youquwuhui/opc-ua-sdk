/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.server.model.variables;

import java.util.Optional;

import com.digitalpetri.opcua.sdk.core.model.UaMandatory;
import com.digitalpetri.opcua.sdk.core.model.variables.CubeItemType;
import com.digitalpetri.opcua.sdk.server.api.UaNamespace;
import com.digitalpetri.opcua.sdk.server.util.UaVariableType;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.structured.AxisInformation;

@UaVariableType(name = "CubeItemType")
public class CubeItemNode extends ArrayItemNode implements CubeItemType {

    public CubeItemNode(UaNamespace namespace,
                        NodeId nodeId,
                        QualifiedName browseName,
                        LocalizedText displayName,
                        Optional<LocalizedText> description,
                        Optional<UInteger> writeMask,
                        Optional<UInteger> userWriteMask,
                        DataValue value,
                        NodeId dataType,
                        Integer valueRank,
                        Optional<UInteger[]> arrayDimensions,
                        UByte accessLevel,
                        UByte userAccessLevel,
                        Optional<Double> minimumSamplingInterval,
                        boolean historizing) {

        super(namespace, nodeId, browseName, displayName, description, writeMask, userWriteMask,
                value, dataType, valueRank, arrayDimensions, accessLevel, userAccessLevel, minimumSamplingInterval, historizing);

    }

    @Override
    @UaMandatory("XAxisDefinition")
    public AxisInformation getXAxisDefinition() {
        Optional<AxisInformation> xAxisDefinition = getProperty("XAxisDefinition");

        return xAxisDefinition.orElse(null);
    }

    @Override
    @UaMandatory("YAxisDefinition")
    public AxisInformation getYAxisDefinition() {
        Optional<AxisInformation> yAxisDefinition = getProperty("YAxisDefinition");

        return yAxisDefinition.orElse(null);
    }

    @Override
    @UaMandatory("ZAxisDefinition")
    public AxisInformation getZAxisDefinition() {
        Optional<AxisInformation> zAxisDefinition = getProperty("ZAxisDefinition");

        return zAxisDefinition.orElse(null);
    }

    @Override
    public synchronized void setXAxisDefinition(AxisInformation xAxisDefinition) {
        getPropertyNode("XAxisDefinition").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(xAxisDefinition)));
        });
    }

    @Override
    public synchronized void setYAxisDefinition(AxisInformation yAxisDefinition) {
        getPropertyNode("YAxisDefinition").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(yAxisDefinition)));
        });
    }

    @Override
    public synchronized void setZAxisDefinition(AxisInformation zAxisDefinition) {
        getPropertyNode("ZAxisDefinition").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(zAxisDefinition)));
        });
    }

}
