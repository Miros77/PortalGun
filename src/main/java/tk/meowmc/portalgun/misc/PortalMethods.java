package tk.meowmc.portalgun.misc;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.my_util.DQuaternion;
import com.qouteall.immersive_portals.portal.GeometryPortalShape;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalExtension;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.ejml.data.FixedMatrix3x3_64F;
import tk.meowmc.portalgun.items.PortalGunItem;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static tk.meowmc.portalgun.items.PortalGunItem.*;

public class PortalMethods {

    public static MinecraftClient client = MinecraftClient.getInstance();
    static FixedMatrix3x3_64F planeMatrix;
    static FixedMatrix3x3_64F planeMatrixInverse;
    static Direction direction;
    static Vec3d positionCorrectionVec;
    static double annoyingNumber1 = 0.7071067690849304;
    static double annoyingNumber2 = 3.0616171314629196E-17;
    static double annoyingNumber3 = 0.7071067094802856;
    static double annoyingNumber4 = 4.329780301713277E-17;
    static double annoyingNumber5 = 2.220446049250313E-16;
    static double annoyingNumber6 = 1.8746996965264928E-33;

    @SuppressWarnings("ReturnOfNull")
    public static Vec3d getDirectionVec(Direction direction)    {
        switch (direction) {
            case UP:
                return new Vec3d(0, -1, 0);
            case DOWN:
                return new Vec3d(0, 1, 0);
            case EAST:
                return new Vec3d(-1, 0, 0);
            case WEST:
                return new Vec3d(1, 0, 0);
            case NORTH:
                return new Vec3d(0, 0, -1);
            case SOUTH:
                return new Vec3d(0, 0, 1);
        }
        return null;
    }

    public static void setPlaneInformation(HitResult hit) {
        planeMatrix = new FixedMatrix3x3_64F();
        planeMatrixInverse = new FixedMatrix3x3_64F();
        planeMatrix.a11 = 0;
        planeMatrix.a22 = 0;
        planeMatrix.a33 = 0;
        planeMatrixInverse.a11 = 0;
        planeMatrixInverse.a22 = 0;
        planeMatrixInverse.a33 = 0;
        direction = ((BlockHitResult) hit).getSide();
        switch (direction) {
            case UP:
            case DOWN:
                planeMatrix.a11 = 1;
                planeMatrix.a23 = 1;
                planeMatrixInverse.a11 = 1;
                planeMatrixInverse.a32 = 1;
                break;
            case EAST:
            case WEST:
                planeMatrix.a13 = 1;
                planeMatrix.a22 = 1;
                planeMatrixInverse.a31 = 1;
                planeMatrixInverse.a22 = 1;
                break;
            case NORTH:
            case SOUTH:
                planeMatrix.a11 = 1;
                planeMatrix.a22 = 1;
                planeMatrixInverse.a11 = 1;
                planeMatrixInverse.a22 = 1;
                break;
        }
        switch (direction) {
            case UP:
                positionCorrectionVec = new Vec3d(0, 0.001, 0);
                break;
            case DOWN:
                positionCorrectionVec = new Vec3d(0, -0.001, 0);
                break;
            case EAST:
                positionCorrectionVec = new Vec3d(0.001, 0, 0);
                break;
            case WEST:
                positionCorrectionVec = new Vec3d(-0.001, 0, 0);
                break;
            case NORTH:
                positionCorrectionVec = new Vec3d(0, 0, -0.001);
                break;
            case SOUTH:
                positionCorrectionVec = new Vec3d(0, 0, 0.001);
                break;
        }
    }

    public static Vec3d multiply(FixedMatrix3x3_64F mat, Vec3d vec) {
        double x = mat.a11 * vec.x + mat.a12 * vec.y + mat.a13 * vec.z;
        double y = mat.a21 * vec.x + mat.a22 * vec.y + mat.a23 * vec.z;
        double z = mat.a31 * vec.x + mat.a32 * vec.y + mat.a33 * vec.z;
        return new Vec3d(x, y, z);
    }

    public static void makeRoundPortal(Portal portal) {
        GeometryPortalShape shape = new GeometryPortalShape();
        final int triangleNum = 30;
        double twoPi = Math.PI * 2;
        shape.triangles = IntStream.range(0, triangleNum)
                .mapToObj(i -> new GeometryPortalShape.TriangleInPlane(
                        0, 0,
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i) / triangleNum),
                        portal.width * 0.5 * Math.cos(twoPi * ((double) i + 1) / triangleNum),
                        portal.height * 0.5 * Math.sin(twoPi * ((double) i + 1) / triangleNum)
                )).collect(Collectors.toList());
        portal.specialShape = shape;
        portal.cullableXStart = 0;
        portal.cullableXEnd = 0;
        portal.cullableYStart = 0;
        portal.cullableYEnd = 0;
    }

    public static Portal Settings1(Direction direction, BlockPos blockPos) {
        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destPos = new Vec3d(blockPos.getX(), blockPos.getY() + 2, blockPos.getZ());

        portal.setDestination(destPos);
        portal.dimensionTo = client.world.getRegistryKey();

        switch (direction) {
            case SOUTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z + 1.001);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z - 0.001);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x - 0.001, portalPosition.y, portalPosition.z + 0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x + 1.001, portalPosition.y, portalPosition.z + 0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y + 1.001, portalPosition.z);
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y - 0.001, portalPosition.z);
                break;
        }


        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar * 1, 0, 0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0, 1, 0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = 1;
        portal.height = 2;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal1";
        PortalExtension portalExtension = PortalExtension.get(portal);
        portalExtension.adjustPositionAfterTeleport = false;
        return portal;
    }

    public static Portal Settings2(Direction direction, BlockPos blockPos) {
        Portal portal = Portal.entityType.create(McHelper.getServer().getWorld(client.world.getRegistryKey()));
        Vec3d portalPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        Vec3d destpos = newPortal1.getPos();

        portal.dimensionTo = newPortal1.world.getRegistryKey();
        portal.setDestination(newPortal1.getPos());
        portal.updatePosition(portalPosition.x, portalPosition.y, portalPosition.z);

        switch (direction) {
            case SOUTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z + 1.001);
                break;
            case NORTH:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y, portalPosition.z - 0.001);
                break;
            case WEST:
                portal.updatePosition(portalPosition.x - 0.001, portalPosition.y, portalPosition.z + 0.5);
                break;
            case EAST:
                portal.updatePosition(portalPosition.x + 1.001, portalPosition.y, portalPosition.z + 0.5);
                break;
            case UP:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y + 1.001, portalPosition.z);
                break;
            case DOWN:
                portal.updatePosition(portalPosition.x + 0.5, portalPosition.y - 0.001, portalPosition.z);
                break;
        }


        Vec3d directionVec = getDirectionVec(direction);
        double scalar = directionVec.x + directionVec.y + directionVec.z;
        Vec3d rightVec = multiply(planeMatrixInverse, new Vec3d(scalar * 1, 0, 0));

        Vec3d axisH = multiply(planeMatrixInverse, new Vec3d(0, 1, 0));

        portal.axisW = rightVec;
        portal.axisH = axisH;
        portal.width = 1;
        portal.height = 2;
        makeRoundPortal(portal);
        portal.portalTag = "portalgun_portal2";
        PortalExtension portalExtension = PortalExtension.get(portal);
        portalExtension.adjustPositionAfterTeleport = false;
        return portal;
    }

    public static Quaternion convertQuaternion(double x, double y, double z, double w) {
        return new DQuaternion(x, y, z, w).toMcQuaternion();
    }

    public static void setRotations(double x1, double y1, double z1, double w1, double x2, double y2, double z2, double w2) {
        newPortal1.rotation = convertQuaternion(x1, y1, z1, w1);
        newPortal2.rotation = convertQuaternion(x2, y2, z2, w2);
    }

    public static void portal1Methods(LivingEntity user, HitResult hit) {
        Direction direction = ((BlockHitResult) hit).getSide();

        PortalPersistentState portalPersistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

        setPlaneInformation(hit);
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal2World = McHelper.getServerWorld(World.OVERWORLD);

        newPortal1 = Settings1(direction, blockPos);
        newPortal1.setDestination(newPortal2.getPos());

        if (newPortal2 != null) {
            portal2World = newPortal2.getOriginWorld();
        }
        Vec3d portal2AxisW = newPortal2.axisW;
        Vec3d portal2AxisH = newPortal2.axisH;


        newPortal2 = Settings2(direction, blockPos);
        newPortal2.updatePosition(newPortal1.getDestPos().getX(), newPortal1.getDestPos().getY(), newPortal1.getDestPos().getZ());
        newPortal2.setDestination(newPortal1.getPos());
        newPortal2.setWorld(portal2World);

        newPortal2.axisW = portal2AxisW;
        newPortal2.axisH = portal2AxisH;

        PortalExtension portal1Extension = PortalExtension.get(newPortal1);
        PortalExtension portal2Extension = PortalExtension.get(newPortal2);


        Quaternion p1Rot = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).hamiltonProduct(PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).getConjugated()).toMcQuaternion();
        Quaternion p2Rot = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).hamiltonProduct(PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).getConjugated()).toMcQuaternion();

        newPortal1.rotation = p1Rot;
        newPortal2.rotation = p2Rot;

        DQuaternion conTest1 = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH);
        DQuaternion conTest2 = conTest1.hamiltonProduct(PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH));
        DQuaternion conTest3 = conTest2.getConjugated();


        if (newPortal1.rotation.getW() == 1 && newPortal2.rotation.getW() == 1 || newPortal1.rotation.getW() == annoyingNumber6 && newPortal2.rotation.getW() == annoyingNumber6) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), 1, p1Rot.getZ(), 0);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), 1, p2Rot.getZ(), 0);
            // setRotations(p1Rot.getX(), 1, p1Rot.getZ(), 0, p2Rot.getX(), 1, p2Rot.getZ(), 0);
        } else if (newPortal1.rotation.getW() == annoyingNumber2 && newPortal2.rotation.getW() == annoyingNumber2) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
            // setRotations(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1, p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        }

        if (newPortal1.rotation.getW() == annoyingNumber1 && newPortal2.rotation.getW() == -annoyingNumber1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), annoyingNumber1);
        } else if (newPortal1.rotation.getW() == -annoyingNumber1 && newPortal2.rotation.getW() == annoyingNumber1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        }

        if (newPortal1.rotation.getW() == annoyingNumber4 && newPortal2.rotation.getW() == annoyingNumber4 || newPortal1.rotation.getW() == annoyingNumber5 && newPortal2.rotation.getW() == annoyingNumber5 || newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = null;
            newPortal2.rotation = null;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
        } else if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1 || newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), annoyingNumber1);
        } else if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), p1Rot.getY(), annoyingNumber1, annoyingNumber1);
        } else if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1 || newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        }

        switch (direction) {
            case WEST:
                newPortal2.setDestination(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 0.5));
                break;
            case EAST:
                newPortal2.setDestination(new Vec3d(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ() + 0.5));
                break;
            case SOUTH:
            case UP:
                newPortal2.setDestination(new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 1));
                break;
            case DOWN:
            case NORTH:
                newPortal2.setDestination(new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ()));
                break;
        }

        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = null;
            newPortal2.rotation = null;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
        } else
        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, -0.5f);
            newPortal2.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, -0.5f);
            newPortal1.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z - 0.999));
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0, 0, 1, 0);
            newPortal2.rotation = convertQuaternion(0, 0, 1, 0);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1, newPortal2.getDestPos().z - 1));
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(0, 0, 1, 0);
            newPortal2.rotation = convertQuaternion(0, 0, 1, 0);
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(-annoyingNumber1, 0, 0, annoyingNumber1);
            newPortal2.rotation = convertQuaternion(0, annoyingNumber3, -annoyingNumber3, 0);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z - 0.999));
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == 1) {
            newPortal2.rotation = convertQuaternion(0, -annoyingNumber1, annoyingNumber1, 0);
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, -annoyingNumber3, 0);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y, newPortal2.getDestPos().z + 0.001));
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else

        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(-annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(0, annoyingNumber3, annoyingNumber3, 0);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.001, newPortal2.getDestPos().z));
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(annoyingNumber2, -annoyingNumber1, -annoyingNumber1, annoyingNumber2);
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, annoyingNumber3, 0);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y, newPortal2.getDestPos().z - 0.001));
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else

        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, 0.5f);
            newPortal2.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, -0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x + 0.001, newPortal2.getDestPos().y, newPortal2.getDestPos().z));
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, 0.5f);
            newPortal1.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, -0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + 1.002, newPortal2.getDestPos().z - 0.999));
        } else

        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, -0.5f);
            newPortal2.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = false;
            portal2Extension.adjustPositionAfterTeleport = true;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x - 0.001, newPortal2.getDestPos().y, newPortal2.getDestPos().z));
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1) {
            newPortal2.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, -0.5f);
            newPortal1.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, 0.5f);
            portal2Extension.adjustPositionAfterTeleport = false;
            portal1Extension.adjustPositionAfterTeleport = true;
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x - 0.001, newPortal2.getDestPos().y - 0.001, newPortal2.getDestPos().z));
        } else


        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(-annoyingNumber3, 0, 0, -annoyingNumber3);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y - 0.001, newPortal2.getDestPos().z));
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = false;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal1.rotation = convertQuaternion(-annoyingNumber3, 0, 0, -annoyingNumber3);
            newPortal2.setDestination(new Vec3d(newPortal2.getDestPos().x, newPortal2.getDestPos().y + -0.001, newPortal2.getDestPos().z));
            portal2Extension.adjustPositionAfterTeleport = true;
            portal1Extension.adjustPositionAfterTeleport = false;
        }

        if (space2BlockState.getBlock().is(Blocks.SNOW) && direction == Direction.UP)
        {
            newPortal1.updatePosition(newPortal1.getX(), newPortal1.getY()-0.875, newPortal1.getZ());
            newPortal2.setDestination(newPortal2.getDestPos().add(0, -0.875, 0));
        }

    }

    public static void portal2Methods(LivingEntity user, HitResult hit) {

        Direction direction = ((BlockHitResult) hit).getSide();

        PortalPersistentState portalPersistentState = McHelper.getServerWorld(user.world.getRegistryKey()).getPersistentStateManager().getOrCreate(() -> new PortalPersistentState(KEY), KEY);

        setPlaneInformation(hit);
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos blockPos = blockHit.getBlockPos();
        World portal1World = McHelper.getServerWorld(World.OVERWORLD);

        if (newPortal1 != null) {
            portal1World = newPortal1.getOriginWorld();
        }
        Vec3d portal1AxisW = newPortal1.axisW;
        Vec3d portal1AxisH = newPortal1.axisH;
        newPortal2 = Settings2(direction, blockPos);


        newPortal1 = Settings1(direction, blockPos);


        newPortal1.updatePosition(newPortal2.getDestPos().getX(), newPortal2.getDestPos().getY(), newPortal2.getDestPos().getZ());
        newPortal1.setDestination(new Vec3d(newPortal2.getX(), newPortal2.getY(), newPortal2.getZ()));
        newPortal1.setWorld(portal1World);
        newPortal1.axisW = portal1AxisW;
        newPortal1.axisH = portal1AxisH;

        PortalExtension portal1Extension = PortalExtension.get(newPortal1);
        PortalExtension portal2Extension = PortalExtension.get(newPortal2);

        Quaternion p1Rot = PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).hamiltonProduct(PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).getConjugated()).toMcQuaternion();
        Quaternion p2Rot = PortalManipulation.getPortalOrientationQuaternion(newPortal2.axisW, newPortal2.axisH).hamiltonProduct(PortalManipulation.getPortalOrientationQuaternion(newPortal1.axisW, newPortal1.axisH).getConjugated()).toMcQuaternion();

        newPortal1.rotation = p1Rot;
        newPortal2.rotation = p2Rot;

        if (PortalGunItem.space2BlockState.getBlock().is(Blocks.SNOW) && direction == Direction.UP)
        {
            newPortal2.updatePosition(newPortal2.getX(), newPortal2.getY()-0.875, newPortal2.getZ());
        }

        if (newPortal1.rotation.getW() == 1 && newPortal2.rotation.getW() == 1 || newPortal1.rotation.getW() == annoyingNumber6 && newPortal2.rotation.getW() == annoyingNumber6) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), 1, p1Rot.getZ(), 0);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), 1, p2Rot.getZ(), 0);
        } else
        if (newPortal1.rotation.getW() == annoyingNumber2 && newPortal2.rotation.getW() == annoyingNumber2) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        }


        if (newPortal1.rotation.getW() == annoyingNumber1 && newPortal2.rotation.getW() == -annoyingNumber1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), annoyingNumber1);
        } else if (newPortal1.rotation.getW() == -annoyingNumber1 && newPortal2.rotation.getW() == annoyingNumber1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        }


        if (newPortal1.rotation.getW() == annoyingNumber4 && newPortal2.rotation.getW() == annoyingNumber4 || newPortal1.rotation.getW() == annoyingNumber5 && newPortal2.rotation.getW() == annoyingNumber5 || newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 || newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = null;
            newPortal2.rotation = null;
        } else
        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), annoyingNumber1);
        } else
        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), annoyingNumber1, p1Rot.getZ(), annoyingNumber1);
            newPortal2.rotation = convertQuaternion(p2Rot.getX(), annoyingNumber1, p2Rot.getZ(), -annoyingNumber1);
        } else

        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(p1Rot.getX(), p1Rot.getY(), annoyingNumber1, annoyingNumber1);
        }

        newPortal1.setDestination(new Vec3d(newPortal2.getX(), newPortal2.getY(), newPortal2.getZ()));

        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, -0.5f);
            newPortal2.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, -0.5f);
            newPortal1.rotation = convertQuaternion(0.5f, 0.5f, -0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0, 0, 1, 0);
            newPortal2.rotation = convertQuaternion(0, 0, 1, 0);
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(0, 0, 1, 0);
            newPortal2.rotation = convertQuaternion(0, 0, 1, 0);
        } else
        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(-annoyingNumber1, 0, 0, annoyingNumber1);
            newPortal2.rotation = convertQuaternion(0, annoyingNumber3, -annoyingNumber3, 0);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == 1) {
            newPortal2.rotation = convertQuaternion(-annoyingNumber1, 0, 0, annoyingNumber1);
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, -annoyingNumber3, 0);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else

        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(-annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(0, annoyingNumber3, annoyingNumber3, 0);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(-annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, annoyingNumber3, 0);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else

        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == -1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, 0.5f);
            newPortal2.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, -0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == -1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, 0.5f);
            newPortal1.rotation = convertQuaternion(0.5f, -0.5f, -0.5f, -0.5f);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else


        if (newPortal1.axisH.y == 1 && newPortal1.axisW.z == 1 && newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1) {
            newPortal1.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, -0.5f);
            newPortal2.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, 0.5f);
            portal1Extension.adjustPositionAfterTeleport = false;
            portal2Extension.adjustPositionAfterTeleport = true;
        } else
        if (newPortal2.axisH.y == 1 && newPortal2.axisW.z == 1 && newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1) {
            newPortal2.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, -0.5f);
            newPortal1.rotation = convertQuaternion(-0.5f, 0.5f, 0.5f, 0.5f);
            portal2Extension.adjustPositionAfterTeleport = false;
            portal1Extension.adjustPositionAfterTeleport = true;
        } else


        if (newPortal1.axisH.z == 1 && newPortal1.axisW.x == 1 && newPortal2.axisH.y == 1 && newPortal2.axisW.x == -1) {
            newPortal1.rotation = convertQuaternion(annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal2.rotation = convertQuaternion(-annoyingNumber3, 0, 0, -annoyingNumber3);
            portal1Extension.adjustPositionAfterTeleport = true;
            portal2Extension.adjustPositionAfterTeleport = false;
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == 1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1) {
            newPortal2.rotation = convertQuaternion(annoyingNumber1, 0, 0, -annoyingNumber1);
            newPortal1.rotation = convertQuaternion(-annoyingNumber3, 0, 0, -annoyingNumber3);
            portal2Extension.adjustPositionAfterTeleport = true;
            portal1Extension.adjustPositionAfterTeleport = false;
        }

        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == -1 /* dmwiamdwadwa */) {
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, annoyingNumber3, 0);
            newPortal2.rotation = convertQuaternion(annoyingNumber2, -annoyingNumber1, -annoyingNumber1, annoyingNumber2);
        } else
        if (newPortal2.axisH.z == 1 && newPortal2.axisW.x == -1 && newPortal1.axisH.y == 1 && newPortal1.axisW.x == 1 /* dmwiamdwadwa */) {
            newPortal1.rotation = convertQuaternion(0, annoyingNumber3, -annoyingNumber3, 0);
            newPortal2.rotation = convertQuaternion(0, -annoyingNumber1, annoyingNumber1, 0);
        }

    }

}
