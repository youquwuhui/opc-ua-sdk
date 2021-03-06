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

package com.digitalpetri.opcua.sdk.server.util;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import com.digitalpetri.opcua.sdk.core.AttributeIds;
import com.digitalpetri.opcua.sdk.core.NumericRange;
import com.digitalpetri.opcua.sdk.core.nodes.DataTypeNode;
import com.digitalpetri.opcua.sdk.core.nodes.MethodNode;
import com.digitalpetri.opcua.sdk.core.nodes.Node;
import com.digitalpetri.opcua.sdk.core.nodes.ObjectNode;
import com.digitalpetri.opcua.sdk.core.nodes.ObjectTypeNode;
import com.digitalpetri.opcua.sdk.core.nodes.ReferenceTypeNode;
import com.digitalpetri.opcua.sdk.core.nodes.VariableNode;
import com.digitalpetri.opcua.sdk.core.nodes.VariableTypeNode;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;

public class AttributeReader {

    private static final Supplier<UaException> ATTRIBUTE_ID_INVALID_EXCEPTION =
            () -> new UaException(StatusCodes.Bad_AttributeIdInvalid);

    public static DataValue readAttribute(Node node, int attribute,
                                          @Nullable TimestampsToReturn timestamps,
                                          @Nullable String indexRange) {
        try {
            DataValue value = readAttribute(node, attribute);

            if (indexRange != null) {
                NumericRange range = NumericRange.parse(indexRange);

                Object valueAtRange = NumericRange.readFromValueAtRange(value.getValue(), range);

                value = new DataValue(
                        new Variant(valueAtRange),
                        value.getStatusCode(),
                        value.getSourceTime(),
                        value.getServerTime());
            }

            if (timestamps != null) {
                value = (attribute == AttributeIds.Value) ?
                        DataValue.derivedValue(value, timestamps) :
                        DataValue.derivedNonValue(value, timestamps);
            }

            return value;
        } catch (UaException e) {
            return new DataValue(e.getStatusCode());
        }
    }

    private static DataValue readAttribute(Node node, int attribute) throws UaException {
        switch (node.getNodeClass()) {
            case DataType:
                return readDataTypeAttribute((DataTypeNode) node, attribute);

            case Method:
                return readMethodAttribute((MethodNode) node, attribute);

            case Object:
                return readObjectAttribute((ObjectNode) node, attribute);

            case ObjectType:
                return readObjectTypeAttribute((ObjectTypeNode) node, attribute);

            case ReferenceType:
                return readReferenceTypeAttribute((ReferenceTypeNode) node, attribute);

            case Variable:
                return readVariableAttribute((VariableNode) node, attribute);

            case VariableType:
                return readVariableTypeAttribute((VariableTypeNode) node, attribute);

            default:
                throw new UaException(StatusCodes.Bad_NodeClassInvalid);
        }

    }

    private static DataValue readNodeAttribute(Node node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.NodeId:
                return dv(node.getNodeId());

            case AttributeIds.NodeClass:
                return dv(node.getNodeClass());

            case AttributeIds.BrowseName:
                return dv(node.getBrowseName());

            case AttributeIds.DisplayName:
                return dv(node.getDisplayName());

            case AttributeIds.Description:
                return node.getDescription().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.WriteMask:
                return node.getWriteMask().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.UserWriteMask:
                return node.getUserWriteMask().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            default:
                throw ATTRIBUTE_ID_INVALID_EXCEPTION.get();
        }
    }

    private static DataValue readDataTypeAttribute(DataTypeNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.IsAbstract:
                return dv(node.getIsAbstract());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readMethodAttribute(MethodNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.Executable:
                return dv(node.isExecutable());

            case AttributeIds.UserExecutable:
                return dv(node.isUserExecutable());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readObjectAttribute(ObjectNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.EventNotifier:
                return dv(node.getEventNotifier());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readObjectTypeAttribute(ObjectTypeNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.IsAbstract:
                return dv(node.getIsAbstract());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readReferenceTypeAttribute(ReferenceTypeNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.IsAbstract:
                return dv(node.getIsAbstract());

            case AttributeIds.Symmetric:
                return dv(node.getSymmetric());

            case AttributeIds.InverseName:
                return node.getInverseName().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readVariableAttribute(VariableNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.Value:
                return node.getValue();

            case AttributeIds.DataType:
                return dv(node.getDataType());

            case AttributeIds.ValueRank:
                return dv(node.getValueRank());

            case AttributeIds.ArrayDimensions:
                return node.getArrayDimensions().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.AccessLevel:
                return dv(node.getAccessLevel());

            case AttributeIds.UserAccessLevel:
                return dv(node.getUserAccessLevel());

            case AttributeIds.MinimumSamplingInterval:
                return node.getMinimumSamplingInterval().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.Historizing:
                return dv(node.getHistorizing());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    private static DataValue readVariableTypeAttribute(VariableTypeNode node, int attribute) throws UaException {
        switch (attribute) {
            case AttributeIds.Value:
                return node.getValue().orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.DataType:
                return dv(node.getDataType());

            case AttributeIds.ValueRank:
                return dv(node.getValueRank());

            case AttributeIds.ArrayDimensions:
                return node.getArrayDimensions().map(AttributeReader::dv)
                        .orElseThrow(ATTRIBUTE_ID_INVALID_EXCEPTION);

            case AttributeIds.IsAbstract:
                return dv(node.getIsAbstract());

            default:
                return readNodeAttribute(node, attribute);
        }
    }

    /** DataValue for a non-value attribute; no source timestamp included. */
    private static DataValue dv(Object o) {
        return new DataValue(new Variant(o), StatusCode.GOOD, null, DateTime.now());
    }

}
