package com.alextim.bee.server.dto;

import com.alextim.bee.server.protocol.DetectorCodes.BDInternalMode;
import com.alextim.bee.server.protocol.DetectorCodes.BDType;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public abstract class InternalData {
    public final int version;
    public final BDType bdType;
    public final long measTime;
    public final BDInternalMode mode;
    public final int reserve;
    public final float[] currentScores;
    public final float[] averageScores;
    public final float temperature;
}
