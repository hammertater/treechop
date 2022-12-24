package ht.treechop.client.model;

import com.mojang.math.Vector3f;
import ht.treechop.common.util.FaceShape;
import ht.treechop.common.util.Vector3;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ModelUtil {
    private static final ResourceLocation UNKNOWN_RESOURCE = new ResourceLocation("treechop", "dynamic");

    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            FaceShape faceShape,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                sprite,
                faceShape.getCorner1(),
                faceShape.getCorner3(),
                orientation,
                culling
        );
    }

    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            Vector3 posFrom,
            Vector3 posTo,
            Direction orientation,
            Direction culling
    ) {
        return makeQuad(
                sprite,
                posFrom,
                posTo,
                orientation,
                culling,
                getUVsForQuad(posFrom, posTo, orientation),
                0
        );
    }

    private static float[] getUVsForQuad(Vector3 posFrom, Vector3 posTo, Direction orientation) {
        Vector3f posFrom3f = posFrom.asVector3f();
        Vector3f posTo3f = posTo.asVector3f();
        return switch (orientation) {
            case UP, DOWN -> new float[]{posFrom3f.x(), posFrom3f.z(), posTo3f.x(), posTo3f.z()};
            case EAST, WEST -> new float[]{posFrom3f.z(), posFrom3f.y(), posTo3f.z(), posTo3f.y()};
            case NORTH, SOUTH -> new float[]{posFrom3f.x(), posFrom3f.y(), posTo3f.x(), posTo3f.y()};
        };
    }

    public static BakedQuad makeQuad(
            TextureAtlasSprite sprite,
            Vector3 posFrom,
            Vector3 posTo,
            Direction orientation,
            Direction culling,
            float[] uvs,
            int uvRotation
    ) {
        return new FaceBakery().bakeQuad(
                posFrom.asVector3f(),
                posTo.asVector3f(),
                new BlockElementFace(culling, -1, "", new BlockFaceUV(uvs, uvRotation)),
                sprite,
                orientation,
                BlockModelRotation.X0_Y0,
                null, // TODO
                true,
                UNKNOWN_RESOURCE
        );
    }
}
