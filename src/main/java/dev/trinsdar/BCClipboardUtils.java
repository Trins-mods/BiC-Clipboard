package dev.trinsdar;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class BCClipboardUtils {
    private static final Vec3 FROM_ORIGIN = new Vec3(-0.5, -0.5, -0.5);
    public static AABB rotate(AABB box, Rotation rotation) {
        return switch (rotation) {
            case NONE -> box;
            case CLOCKWISE_90 -> new AABB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX);
            case CLOCKWISE_180 -> new AABB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case COUNTERCLOCKWISE_90 -> new AABB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
        };
    }

    public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
        return rotate(shape, box -> rotate(box, rotation));
    }

    public static VoxelShape rotate(VoxelShape shape, UnaryOperator<AABB> rotateFunction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        for (AABB sourceBoundingBox : shape.toAabbs()) {
            rotatedPieces.add(Shapes.create(rotateFunction.apply(sourceBoundingBox.move(FROM_ORIGIN.x, FROM_ORIGIN.y, FROM_ORIGIN.z)).move(-FROM_ORIGIN.x, -FROM_ORIGIN.z, -FROM_ORIGIN.z)));
        }
        return combine(rotatedPieces.toArray(new VoxelShape[0]));
    }

    public static VoxelShape combine(VoxelShape... shapes) {
        return batchCombine(Shapes.empty(), BooleanOp.OR, true, shapes);
    }

    public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, VoxelShape... shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.joinUnoptimized(combinedShape, shape, function);
        }
        return simplify ? combinedShape.optimize() : combinedShape;
    }

    public static <T> List<T> extend(List<T> list, int size, T fill) {
        for (int i = list.size(); i < size; i++) {
            list.add(fill);
        }
        return list;
    }
}
