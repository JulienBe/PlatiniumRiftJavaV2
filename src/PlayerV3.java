import java.util.*;

/***
 *    ██████╗ ██╗      █████╗ ██╗   ██╗███████╗██████╗
 *    ██╔══██╗██║     ██╔══██╗╚██╗ ██╔╝██╔════╝██╔══██╗
 *    ██████╔╝██║     ███████║ ╚████╔╝ █████╗  ██████╔╝
 *    ██╔═══╝ ██║     ██╔══██║  ╚██╔╝  ██╔══╝  ██╔══██╗
 *    ██║     ███████╗██║  ██║   ██║   ███████╗██║  ██║
 *    ╚═╝     ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝  ╚═╝
 *
 */
class PlayerV3 {

    static final float FIRST_TURN_DIVISION = 2;
    // Je veux du java 8 bordel !
    private static long longestUpdate = 0, longestSpawn = 0, longestMvt = 0;
    public static final int DRONE_COST = 20;
    public static int playerCount, myId;
    public static final Random R = new Random();
    public static boolean firstTurn = true;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        final World3 world3 = init(in);

        while (true) {
            int platinum = in.nextInt(); // my available Platinum
            // pas vraiment de changement notable
//            if (firstTurn)
//                platinum /= FIRST_TURN_DIVISION;
            in.nextLine();
            long begin = System.currentTimeMillis();
            /***
             *     _   _           _       _
             *    | | | |         | |     | |
             *    | | | |_ __   __| | __ _| |_ ___
             *    | | | | '_ \ / _` |/ _` | __/ _ \
             *    | |_| | |_) | (_| | (_| | ||  __/
             *     \___/| .__/ \__,_|\__,_|\__\___|
             *          | |
             *          |_|
             */
            world3.update(in);
            long update = System.currentTimeMillis() - begin;
            System.err.println("update : " + update + "/" + longestUpdate);
            if (update > longestUpdate)
                longestUpdate = update;
            begin = System.currentTimeMillis();
            /***
             *    ___  ___                                    _
             *    |  \/  |                                   | |
             *    | .  . | _____   _____ _ __ ___   ___ _ __ | |_ ___
             *    | |\/| |/ _ \ \ / / _ \ '_ ` _ \ / _ \ '_ \| __/ __|
             *    | |  | | (_) \ V /  __/ | | | | |  __/ | | | |_\__ \
             *    \_|  |_/\___/ \_/ \___|_| |_| |_|\___|_| |_|\__|___/
             *
             *
             */
            world3.movements();
            long mvt = System.currentTimeMillis() - begin;
            System.err.println("mvt : " + mvt + "/" + longestMvt);
            if (mvt > longestMvt)
                longestMvt = mvt;
            begin = System.currentTimeMillis();
            /***
             *     _____
             *    /  ___|
             *    \ `--. _ __   __ ___      ___ __
             *     `--. \ '_ \ / _` \ \ /\ / / '_ \
             *    /\__/ / |_) | (_| |\ V  V /| | | |
             *    \____/| .__/ \__,_| \_/\_/ |_| |_|
             *          | |
             *          |_|
             */
            world3.spawnDrones(platinum);
            long spawn = System.currentTimeMillis() - begin;
            System.err.println("spawn : " + spawn + "/" + longestSpawn);
            if (spawn > longestSpawn)
                longestSpawn = spawn;
            System.err.println("Spawn : " + (System.currentTimeMillis() - begin));
            firstTurn = false;
        }
    }

    private static World3 init(Scanner in) {
        playerCount = in.nextInt(); // the amount of players (2 to 4)
        myId = in.nextInt(); // my player ID (0, 1, 2 or 3)
        int zoneCount = in.nextInt(); // the amount of zones on the map
        int linkCount = in.nextInt(); // the amount of links between all
        in.nextLine();
        World3 world3 = new World3();
        world3.init(in, zoneCount, linkCount);
        return world3;
    }

}

/***
 *    ██╗    ██╗ ██████╗ ██████╗ ██╗     ██████╗
 *    ██║    ██║██╔═══██╗██╔══██╗██║     ██╔══██╗
 *    ██║ █╗ ██║██║   ██║██████╔╝██║     ██║  ██║
 *    ██║███╗██║██║   ██║██╔══██╗██║     ██║  ██║
 *    ╚███╔███╔╝╚██████╔╝██║  ██║███████╗██████╔╝
 *     ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═════╝
 *
 */
class World3 {

    // 0.85 : 52/772 -- 40.19
    private static final float SPAWN_ATTRACTION_DIMINISHING = 0.50f;
    private int zoneCount;
    private final List<ContinentAlt> continentAlts = new ArrayList<>(), disputed = new ArrayList<>();

    void init(Scanner in, int zoneCount, int linkCount) {
        Map<Integer, ZoneAlt> zones = new HashMap<>();
        this.zoneCount = zoneCount;
        for (int i = 0; i < zoneCount; i++) {
            int zoneId = in.nextInt();
            int platinumSource = in.nextInt();
            ZoneAlt zoneAlt = new ZoneAlt(platinumSource, zoneId);
            zones.put(zoneId, zoneAlt);
            in.nextLine();
        }

        for (int i = 0; i < linkCount; i++) {
            int zone1 = in.nextInt();
            int zone2 = in.nextInt();
            ZoneAlt z1 = zones.get(zone1);
            ZoneAlt z2 = zones.get(zone2);
            z1.addAdjacentZone(z2);
            z2.addAdjacentZone(z1);
            in.nextLine();
        }

        for (ZoneAlt z : zones.values()) {
            ContinentAlt continentAlt = getRattachedContinent(z);
            if (continentAlt ==  null) {
                continentAlt = getPossibleContinent(z);
                if (continentAlt == null) {
                    continentAlt = new ContinentAlt();
                    continentAlts.add(continentAlt);
                }
                continentAlt.addZone(z);
            }
        }
        for (ContinentAlt c : continentAlts)
            c.initFinished();
        disputed.addAll(continentAlts);
    }

    private ContinentAlt getPossibleContinent(ZoneAlt zoneAlt) {
        for (ZoneAlt z : zoneAlt.getAllAdjacentZones()) {
            ContinentAlt c = getRattachedContinent(z);
            if (c != null)
                return c;
        }
        return null;
    }

    private ContinentAlt getRattachedContinent(ZoneAlt zoneAlt) {
        for (ContinentAlt c : continentAlts)
            if (c.zones.containsValue(zoneAlt))
                return c;
        return null;
    }

    private void addContinentToList(ContinentAlt continentAlt) {
        if (continentAlt.status == ContinentStatusAlt.DISPUTED)   disputed.add(continentAlt);
        else                                                disputed.remove(continentAlt);
    }

    void update(Scanner in) {
        for (ContinentAlt c : continentAlts)
            c.newTurn();
        for (int i = 0; i < zoneCount; i++)
            updateZone(in);

        disputed.clear();

        for (ContinentAlt c : continentAlts) {
            UtilsAlt.determineStatus(c);
            addContinentToList(c);
            c.updateFinished();
        }
    }

    private void updateZone(Scanner in) {
        int zId = in.nextInt(); // this zoneAlt's ID
        int ownerId = in.nextInt(); // the player who owns this zoneAlt (-1 otherwise)
        int podsP0 = in.nextInt(); // player 0's PODs on this zoneAlt
        int podsP1 = in.nextInt(); // player 1's PODs on this zoneAlt
        int podsP2 = in.nextInt(); // player 2's PODs on this zoneAlt (always 0 for a two player game)
        int podsP3 = in.nextInt(); // player 3's PODs on this zoneAlt (always 0 for a two or three player game)
        in.nextLine();

        for (ContinentAlt c : continentAlts)
            c.update(zId, ownerId, podsP0, podsP1, podsP2, podsP3);
    }

    /***
     *    ___  ___                                    _
     *    |  \/  |                                   | |
     *    | .  . | _____   _____ _ __ ___   ___ _ __ | |_ ___
     *    | |\/| |/ _ \ \ / / _ \ '_ ` _ \ / _ \ '_ \| __/ __|
     *    | |  | | (_) \ V /  __/ | | | | |  __/ | | | |_\__ \
     *    \_|  |_/\___/ \_/ \___|_| |_| |_|\___|_| |_|\__|___/
     *
     *
     */
    void movements() {
        List<CommandMvtAlt> commands = new ArrayList<>();

        List<ZoneAlt> zonesWithDrones = new ArrayList<>();
        for (ContinentAlt c : disputed)
            zonesWithDrones.addAll(c.zoneAltWithDrones);
        Iterator<ZoneAlt> it = zonesWithDrones.iterator();
        while (it.hasNext()) {
            ZoneAlt origin = it.next();
            int pop = 0;
            int i = origin.podsToKeep();
            for (; i < origin.getDrones(); i++) {
                ZoneAlt zoneAltToGo = origin.getAdjacenToGoTo();
                if (zoneAltToGo != null) {
                    pop++;
                    sendDrone(commands, origin, zoneAltToGo, zoneAltToGo);
                    System.err.println("Adjacent : " + origin.id + " -> " + zoneAltToGo.id + " with future drones : " + zoneAltToGo.futurDrones);
                }
            }
            if (origin.getDrones() == pop)
                it.remove();
        }
        Iterator<ZoneAlt> itDistant = zonesWithDrones.iterator();
        while (itDistant.hasNext()) {
            ZoneAlt origin = itDistant.next();
            int i = origin.podsToKeep();
            for (; i < origin.getDrones(); i++) {
                MagnetismResolverAlt magnetismResolver = origin.getDistantToGoTo();
                if (magnetismResolver != null) {
                    sendDrone(commands, origin, magnetismResolver.adjacent, magnetismResolver.target);
                    System.err.println("Distant : " + origin.id + " -> " + magnetismResolver.adjacent.id + "    TARGET : " + magnetismResolver.target.id);
                }
            }
        }
        UtilsAlt.executeCommands(commands);
    }

    private void sendDrone(List<CommandMvtAlt> commands, ZoneAlt z, ZoneAlt zoneAltToGo, ZoneAlt target) {
        CommandMvtAlt commandMvtAlt = new CommandMvtAlt();
        commandMvtAlt.from = z.id;
        commandMvtAlt.to = zoneAltToGo.id;
        commandMvtAlt.drones = 1;
        commands.add(commandMvtAlt);
        zoneAltToGo.futurDrones++;
        target.targetted++;
    }


    /***
     *     _____
     *    /  ___|
     *    \ `--.  _ __    __ _ __      __ _ __
     *     `--. \| '_ \  / _` |\ \ /\ / /| '_ \
     *    /\__/ /| |_) || (_| | \ V  V / | | | |
     *    \____/ | .__/  \__,_|  \_/\_/  |_| |_|
     *           | |
     *           |_|
     *
     */

    void spawnDrones(int platinum) {
        List<CommandSpawnAlt> commands = new ArrayList<>();

        TreeSet<ContinentSpawnAnalyticAlt> spawnAnalytics = new TreeSet<>();
        for (ContinentAlt c : disputed)
            spawnAnalytics.add(c.getSpawnAnalytics());

        if (spawnAnalytics.isEmpty())
            return;
        while (platinum >= PlayerV3.DRONE_COST)
            platinum = spawnDrone(platinum, commands, spawnAnalytics);

        UtilsAlt.executeCommands(commands);
    }

    private int spawnDrone(int platinum, List<CommandSpawnAlt> commands, TreeSet<ContinentSpawnAnalyticAlt> spawnAnalytics) {
        ContinentSpawnAnalyticAlt spawnAnalytic = spawnAnalytics.pollFirst();
        SpawnResolverAlt candidate = spawnAnalytic.pollBest();

        CommandSpawnAlt commandSpawn = new CommandSpawnAlt();
        commandSpawn.drones = 1;
        if (PlayerV3.firstTurn)
            commandSpawn.drones++;
        commandSpawn.to = candidate.zoneAlt.id;

        candidate.magnetism *= SPAWN_ATTRACTION_DIMINISHING;
        spawnAnalytic.continentAlt.futurDrones += commandSpawn.drones;
        if (spawnAnalytic.continentAlt.futurDrones == 1)
            spawnAnalytic.totalValue /= ContinentAlt.NO_DRONES_SPAWN_MULTI;
        commands.add(commandSpawn);
        spawnAnalytic.addCandidate(candidate);
        spawnAnalytics.add(spawnAnalytic);
        return platinum - PlayerV3.DRONE_COST;
    }


}

/***
 *     ██████╗ ██████╗ ███╗   ██╗████████╗██╗███╗   ██╗███████╗███╗   ██╗████████╗███████╗
 *    ██╔════╝██╔═══██╗████╗  ██║╚══██╔══╝██║████╗  ██║██╔════╝████╗  ██║╚══██╔══╝██╔════╝
 *    ██║     ██║   ██║██╔██╗ ██║   ██║   ██║██╔██╗ ██║█████╗  ██╔██╗ ██║   ██║   ███████╗
 *    ██║     ██║   ██║██║╚██╗██║   ██║   ██║██║╚██╗██║██╔══╝  ██║╚██╗██║   ██║   ╚════██║
 *    ╚██████╗╚██████╔╝██║ ╚████║   ██║   ██║██║ ╚████║███████╗██║ ╚████║   ██║   ███████║
 *     ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝   ╚═╝   ╚═╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚══════╝
 *
 */

class ContinentAlt {

    static final float NO_DRONES_SPAWN_MULTI = 1;
    int ressources = 0, futurDrones = 0, otherPlayersActive;
    int[] drones = new int[4];
    ContinentStatusAlt status = ContinentStatusAlt.DISPUTED;
    Map<Integer, ZoneAlt> zones = new HashMap<>(), neutralZones = new HashMap<>(), hostileZones = new HashMap<>(), controlledZones = new HashMap<>();
    List<ZoneAlt> zoneAltWithDrones = new ArrayList<>();

    void addZone(ZoneAlt z) {
        zones.put(z.id, z);
        neutralZones.put(z.id, z);
        ressources += z.platinium;
        z.continentAlt = this;
    }

    public void initFinished() {
        for (ZoneAlt z : zones.values())
            z.initFinished();
    }

    public void newTurn() {
        for (int i = 0; i < drones.length; i++)
            drones[i] += 0;
        futurDrones = 0;
        zoneAltWithDrones.clear();
    }
    /**
     * @return true if the zoneAlt has been updated
     */
    boolean update(int zId, int ownerId, int podsP0, int podsP1, int podsP2, int podsP3) {
        if (zones.containsKey(zId)) {
            ZoneAlt zoneAlt = zones.get(zId);
            ZoneStatusAlt previousStatus = zoneAlt.status;
            ZoneStatusAlt newStatus = zoneAlt.update(ownerId, podsP0, podsP1, podsP2, podsP3);
            if (previousStatus != newStatus)
                UtilsAlt.zoneStatusChanged(this, previousStatus, newStatus, zoneAlt);
            drones[0] += podsP0;
            drones[1] += podsP1;
            drones[2] += podsP2;
            drones[3] += podsP3;
            if (drones[PlayerV3.myId] > 0)
                zoneAltWithDrones.add(zoneAlt);
            return true;
        }
        return false;
    }

    void updateFinished() {
        otherPlayersActive = UtilsAlt.getOtherPlayerActive(drones);
        for (ZoneAlt z : zones.values())
            z.updateFinished();
    }

    /***
     *      ___
     *     / __|  _ __   __ _  __ __ __  _ _
     *     \__ \ | '_ \ / _` | \ V  V / | ' \
     *     |___/ | .__/ \__,_|  \_/\_/  |_||_|
     *           |_|
     */


    public ContinentSpawnAnalyticAlt getSpawnAnalytics() {
        ContinentSpawnAnalyticAlt spawnAnalytic = new ContinentSpawnAnalyticAlt(this);
        for (ZoneAlt z : neutralZones.values())
            spawnAnalytic.addCandidate(getSpawnResolverFreeZones(z, UtilsAlt.lessDrones(drones)));
        for (ZoneAlt z : controlledZones.values())
            spawnAnalytic.addCandidate(getSpawnResolverMyZones(z, UtilsAlt.lessDrones(drones)));
        if (drones[PlayerV3.myId] == 0)
            spawnAnalytic.totalValue *= NO_DRONES_SPAWN_MULTI;
        return spawnAnalytic;
    }

    private SpawnResolverAlt getSpawnResolverFreeZones(ZoneAlt z, boolean lessDrones) {
        z.spawnResolver.magnetism = z.evaluateFreeZone(otherPlayersActive, lessDrones);
        return z.spawnResolver;
    }
    private SpawnResolverAlt getSpawnResolverMyZones(ZoneAlt z, boolean lessDrones) {
        z.spawnResolver.magnetism = z.evaluateMyZone(otherPlayersActive, lessDrones);
        return z.spawnResolver;
    }

    /***
     *     ___      ___     ______     ___      ___   _______   ___      ___   _______   _____  ___    ___________    ________
     *    |"  \    /"  |   /    " \   |"  \    /"  | /"     "| |"  \    /"  | /"     "| (\"   \|"  \  ("     _   ")  /"       )
     *     \   \  //   |  // ____  \   \   \  //  / (: ______)  \   \  //   |(: ______) |.\\   \    |  )__/  \\__/  (:   \___/
     *     /\\  \/.    | /  /    ) :)   \\  \/. ./   \/    |    /\\  \/.    | \/    |   |: \.   \\  |     \\_ /      \___  \
     *    |: \.        |(: (____/ //     \.    //    // ___)_  |: \.        | // ___)_  |.  \    \. |     |.  |       __/  \\
     *    |.  \    /:  | \        /       \\   /    (:      "| |.  \    /:  |(:      "| |    \    \ |     \:  |      /" \   :)
     *    |___|\__/|___|  \"_____/         \__/      \_______) |___|\__/|___| \_______)  \___|\____\)      \__|     (_______/
     *
     */




}

/***
 *    ███████╗ ██████╗ ███╗   ██╗███████╗
 *    ╚══███╔╝██╔═══██╗████╗  ██║██╔════╝
 *      ███╔╝ ██║   ██║██╔██╗ ██║█████╗
 *     ███╔╝  ██║   ██║██║╚██╗██║██╔══╝
 *    ███████╗╚██████╔╝██║ ╚████║███████╗
 *    ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝
 *
 */

class ZoneAlt {

    // SPAWN
    private static final float
            MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE = 12,
    //*2 => 52ème / 40.30
    ADJACENT_ZONE_SIZE_DIV = 2;
    // MVT
    private static final int MAX_DISTANCE = 7, MAX_DRONES = 5;


    ZoneStatusAlt status = ZoneStatusAlt.NEUTRAL;
    // /!\
    SpawnResolverAlt spawnResolver = new SpawnResolverAlt(this);
    final int id;
    int platinium, ownerId = -1, platiniumNearby = 0, nbEnemies, futurDrones = 0, targetted = 0;
    int[] drones = new int[4], adjacentDrones = new int[4];
    List<ZoneAlt> adjacentZoneAlts = new ArrayList<>(), adjacentWithRessources = new ArrayList<>(), adjacentOfAdjacentWithRessources = new ArrayList<>();
    boolean justBeenTaken = false;
    public ContinentAlt continentAlt;

    ZoneAlt(int platinium, int id) {
        this.platinium = platinium;
        this.id = id;
    }

    void addAdjacentZone(ZoneAlt zoneAlt) {
        adjacentZoneAlts.add(zoneAlt);
        if (zoneAlt.platinium > 0) {
            adjacentWithRessources.add(zoneAlt);
            platiniumNearby += zoneAlt.platinium;
        }
    }

    public void initFinished() {
        for (ZoneAlt z : adjacentZoneAlts) {
            for (ZoneAlt distant : z.adjacentWithRessources) {
                if (!distant.equals(this))
                    adjacentOfAdjacentWithRessources.add(distant);
            }
        }
    }

    ZoneStatusAlt update(int ownerId, int podsP0, int podsP1, int podsP2, int podsP3) {
        justBeenTaken = this.ownerId == PlayerV3.myId && ownerId != this.ownerId;
        this.ownerId = ownerId;
        drones[0] = podsP0;
        drones[1] = podsP1;
        drones[2] = podsP2;
        drones[3] = podsP3;
        reset();
        for (int i = 0; i < drones.length; i++) {
            if (i == PlayerV3.myId)
                continue;
            nbEnemies += drones[i];
        }
        return UtilsAlt.determineStatus(this);
    }

    private void reset() {
        futurDrones = 0;
        nbEnemies = 0;
        targetted = 0;
    }

    int getDrones() {
        return drones[PlayerV3.myId];
    }

    List<ZoneAlt> getAllAdjacentZones() {
        List<ZoneAlt> zoneAlts = new ArrayList<>();
        return createContinent(zoneAlts);
    }

    private List<ZoneAlt> createContinent(List<ZoneAlt> zoneAlts) {
        zoneAlts.add(this);
        for (ZoneAlt z : adjacentZoneAlts)
            if (!zoneAlts.contains(z))
                z.createContinent(zoneAlts);
        return zoneAlts;
    }

    public void updateFinished() {
        for (int i = 0; i < adjacentDrones.length; i++)
            adjacentDrones[i] = 0;
        for (ZoneAlt z : adjacentZoneAlts)
            for (int i = 0; i < adjacentDrones.length; i++)
                adjacentDrones[i] += z.drones[i];
    }

    /***
     *     ___      ___     ______     ___      ___   _______   ___      ___   _______   _____  ___    ___________    ________
     *    |"  \    /"  |   /    " \   |"  \    /"  | /"     "| |"  \    /"  | /"     "| (\"   \|"  \  ("     _   ")  /"       )
     *     \   \  //   |  // ____  \   \   \  //  / (: ______)  \   \  //   |(: ______) |.\\   \    |  )__/  \\__/  (:   \___/
     *     /\\  \/.    | /  /    ) :)   \\  \/. ./   \/    |    /\\  \/.    | \/    |   |: \.   \\  |     \\_ /      \___  \
     *    |: \.        |(: (____/ //     \.    //    // ___)_  |: \.        | // ___)_  |.  \    \. |     |.  |       __/  \\
     *    |.  \    /:  | \        /       \\   /    (:      "| |.  \    /:  |(:      "| |    \    \ |     \:  |      /" \   :)
     *    |___|\__/|___|  \"_____/         \__/      \_______) |___|\__/|___| \_______)  \___|\____\)      \__|     (_______/
     *
     */

    /**
     * - ressources à prendre
     * ou
     * - case à prendre sans risque
     *
     * @return
     */
    public ZoneAlt getAdjacenToGoTo() {
        ZoneAlt zoneAlt = null;
        if (platinium > 0 && UtilsAlt.hasEnemiesNearby(this) && getDrones() < MAX_DRONES)
            return this;
        int best = 0;
        for (ZoneAlt z : adjacentWithRessources) {
            if (z.futurDrones != 0
                    ||
                    UtilsAlt.isMine(z)
                    ||
                    ((UtilsAlt.hasEnemies(z) && getDrones() < MAX_DRONES)
                    ||
                    UtilsAlt.hasEnemiesNearby(z)))
                continue;
            if (zoneAlt == null) {
                zoneAlt = z;
                best = z.platinium;
            } else if (best < z.platinium) {
                zoneAlt = z;
                best = z.platinium;
            }
        }
        if (getDrones() > MAX_DRONES)
            if (zoneAlt == null) {
                for (ZoneAlt z : adjacentZoneAlts)
                    if (!UtilsAlt.isMine(z) && getDrones() > UtilsAlt.getEnemieDrones(z.drones) + 2)
                        return z;
            }

        return zoneAlt;
    }

    public MagnetismResolverAlt getDistantToGoTo() {
        if (platinium > 0 && UtilsAlt.hasEnemiesNearby(this)) {
            MagnetismResolverAlt magnetismResolver = new MagnetismResolverAlt();
            magnetismResolver.adjacent = this;
            magnetismResolver.target = this;
            return magnetismResolver;
        }
        TreeSet<MagnetismResolverAlt> candidates = new TreeSet<>();
        for (ZoneAlt z : adjacentZoneAlts)
            z.examineZone(getDrones(), candidates, z, id);
        if (candidates.size() > 0)
            return candidates.first();
        int max = 0;
        MagnetismResolverAlt magnetismResolver = new MagnetismResolverAlt();
        for (ZoneAlt z : adjacentZoneAlts)
            if (z.adjacentZoneAlts.size() > max) {
                magnetismResolver.adjacent = z;
                magnetismResolver.target = z;
                max = z.adjacentZoneAlts.size();
            }
        if (magnetismResolver.adjacent != null)
            return magnetismResolver;
        return null;
    }

    private void examineZone(int drones, TreeSet<MagnetismResolverAlt> candidates, ZoneAlt adjacent, int... ids) {
        for (int i : ids)
            if (i == id)
                return;
        if (UtilsAlt.getEnemieDrones(this.drones) >= drones)
            return;
        float magnetism = getMagnetism();
        int[] newIds = new int[ids.length + 1];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        newIds[newIds.length - 1] = id;
        if (magnetism > 0)
            candidates.add(new MagnetismResolverAlt(magnetism / newIds.length * 2, adjacent, this));

        if (newIds.length >= MAX_DISTANCE)
            return;

        for (ZoneAlt z : adjacentZoneAlts)
            z.examineZone(drones, candidates, adjacent, newIds);
    }

    private int getMagnetism() {
        if (UtilsAlt.getOtherPlayerActive(adjacentDrones) > 1)
            return -1;
        int i = -futurDrones;
        if (UtilsAlt.isMine(this)) {
            if (UtilsAlt.isBorder(this))
                i++;
            return i;
        }
        if (!UtilsAlt.hasEnemies(this))
            i += 5 + platinium * 5;
        if (platinium > 0)
            i /= UtilsAlt.getNbEnemieZonesNearby(this) + 1;
        //i /= futurDrones + 1;
        return i;
    }

    /***
     *     _____
     *    /  ___|
     *    \ `--.  _ __    __ _ __      __ _ __
     *     `--. \| '_ \  / _` |\ \ /\ / /| '_ \
     *    /\__/ /| |_) || (_| | \ V  V / | | | |
     *    \____/ | .__/  \__,_|  \_/\_/  |_| |_|
     *           | |
     *           |_|
     *
     * avant other player active : 64
     * apres : 130
     * j'ai chipoté aussi dans le has enemies nearby, is mine etc, genre le value /= 2, enfin me semble... putain des commits
     **/

    public int evaluateFreeZone(int otherPlayerActive, boolean lessDrones) {
        int value = 0;

        for (ZoneAlt z : adjacentWithRessources) {
            if (!UtilsAlt.hasEnemies(z) && !UtilsAlt.isMine(z)) {
                value += (z.platinium * 2) / (1 + z.futurDrones);
                if (!UtilsAlt.hasEnemiesNearby(z))
                    value += (z.platinium * 2) / (1 + z.futurDrones);
            }
        }

        for (ZoneAlt z : adjacentOfAdjacentWithRessources)
            if (!UtilsAlt.isMine(z) && !UtilsAlt.hasEnemies(z))
                value += (z.platinium) / (1 + z.futurDrones);

        if (futurDrones == 0 && UtilsAlt.isFree(this))
            value += platinium * MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE;


        value *= 7 - adjacentZoneAlts.size();
        if (UtilsAlt.isMine(this) && UtilsAlt.hasEnemiesNearby(this))
            value *= platinium + 1;
        else
            value /= UtilsAlt.getNbEnemieZonesNearby(this) + 1;
        value /= futurDrones*2 + 1;
        // Ca ça fait gagner plein de places
        return value;
//
//        int value = 0;
//
//        for (ZoneAlt z : adjacentWithRessources) {
//            if (!UtilsAlt.hasEnemies(z) && !UtilsAlt.isMine(z)) {
//                value += (z.platinium * 2) / (1 + z.futurDrones);
//                if (!UtilsAlt.hasEnemiesNearby(z))
//                    value += (z.platinium * 2) / (1 + z.futurDrones);
//            }
//        }
//
//        for (ZoneAlt z : adjacentOfAdjacentWithRessources)
//            if (!UtilsAlt.isMine(z) && !UtilsAlt.hasEnemies(z))
//                value += (z.platinium * 2) / (1 + z.futurDrones);
//
//        value += platinium * MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE;
//        value /= futurDrones + 1;
//        // Ca ça fait gagner plein de places
//        value *= 7 - adjacentZoneAlts.size();
//
//        return value;
    }



    public int evaluateMyZone(int otherPlayerActive, boolean lessDrones) {
        return evaluateFreeZone(otherPlayerActive, lessDrones);
//        int value = 0;
//        if (UtilsAlt.isBorder(this))
//            value += 2;
//        for (ZoneAlt z : adjacentWithRessources)
//            if (UtilsAlt.isFree(z) && !UtilsAlt.hasEnemiesNearby(z))
//                value += (z.platinium * 4) / (1 + z.futurDrones);
//
//        for (ZoneAlt z : adjacentOfAdjacentWithRessources)
//            if (!UtilsAlt.isMine(z) && !UtilsAlt.hasEnemies(z))
//                value += (z.platinium) / ( 1 + z.futurDrones);
//
//        value /= futurDrones + 1;
//
//        if (UtilsAlt.hasEnemiesNearby(this))
//            value *= 1 + platinium * 2;
//
//        return value;
    }

    public boolean shouldDefend() {
        return platinium > 0 && UtilsAlt.hasEnemiesNearby(this);
    }

    public int podsToKeep() {
        if (platinium == 0)
            return 0;
        int spare = getDrones() - UtilsAlt.getNbEnemieZonesNearby(this);
        if (spare <= 0)
            return 0;
        return getDrones() - spare;
    }


}

/***
 *     ██████╗ ██████╗ ███╗   ███╗███╗   ███╗ █████╗ ███╗   ██╗██████╗ ███████╗
 *    ██╔════╝██╔═══██╗████╗ ████║████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝
 *    ██║     ██║   ██║██╔████╔██║██╔████╔██║███████║██╔██╗ ██║██║  ██║███████╗
 *    ██║     ██║   ██║██║╚██╔╝██║██║╚██╔╝██║██╔══██║██║╚██╗██║██║  ██║╚════██║
 *    ╚██████╗╚██████╔╝██║ ╚═╝ ██║██║ ╚═╝ ██║██║  ██║██║ ╚████║██████╔╝███████║
 *     ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═════╝ ╚══════╝
 *
 */

class CommandMvtAlt {
    int drones, from, to;
    @Override    public String toString() {        return drones + " " + from + " " + to + " ";    }
}
class CommandSpawnAlt {
    int drones, to;
    @Override    public String toString() {        return drones + " " + to + " ";    }
}

/***
 *     ██████╗ ████████╗██╗  ██╗███████╗██████╗
 *    ██╔═══██╗╚══██╔══╝██║  ██║██╔════╝██╔══██╗
 *    ██║   ██║   ██║   ███████║█████╗  ██████╔╝
 *    ██║   ██║   ██║   ██╔══██║██╔══╝  ██╔══██╗
 *    ╚██████╔╝   ██║   ██║  ██║███████╗██║  ██║
 *     ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
 */

class MagnetismResolverAlt implements Comparable<MagnetismResolverAlt>{
    float magnetism;
    ZoneAlt adjacent, target;

    public MagnetismResolverAlt(float magnetism, ZoneAlt adjacent, ZoneAlt target) {
        this.magnetism = magnetism;
        this.adjacent = adjacent;
        this.target = target;
    }

    public MagnetismResolverAlt() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MagnetismResolverAlt that = (MagnetismResolverAlt) o;

        if (Float.compare(that.magnetism, magnetism) != 0) return false;
        if (adjacent != null ? !adjacent.equals(that.adjacent) : that.adjacent != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (magnetism != +0.0f ? Float.floatToIntBits(magnetism) : 0);
        result = 31 * result + (adjacent != null ? adjacent.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(MagnetismResolverAlt o) {
        return (int) ((o.magnetism * 1000) - (magnetism * 1000));
    }
}

class ContinentSpawnAnalyticAlt implements Comparable<ContinentSpawnAnalyticAlt> {
    TreeSet<SpawnResolverAlt> candidates = new TreeSet<>();
    int totalValue;
    final ContinentAlt continentAlt;

    public ContinentSpawnAnalyticAlt(ContinentAlt continentAlt) {
        this.continentAlt = continentAlt;
    }

    public void addCandidate(SpawnResolverAlt spawnResolver) {
        totalValue += spawnResolver.magnetism;
        candidates.add(spawnResolver);
    }

    public SpawnResolverAlt pollBest() {
        SpawnResolverAlt spawnResolver = candidates.pollFirst();
        totalValue -= spawnResolver.magnetism;
        return spawnResolver;
    }

    @Override
    public int compareTo(ContinentSpawnAnalyticAlt o) {
        return o.totalValue * 1000 - totalValue * 1000;
    }

}

class SpawnResolverAlt implements Comparable<SpawnResolverAlt> {
    int magnetism;
    final ZoneAlt zoneAlt;

    public SpawnResolverAlt(ZoneAlt zoneAlt) {
        this.zoneAlt = zoneAlt;
    }

    void reset() {
        magnetism = -500;
    }

    @Override
    public int compareTo(SpawnResolverAlt o) {
        return o.magnetism - magnetism;
    }
}


enum ContinentStatusAlt {      PACIFIED, HOSTILE, DISPUTED;                        }
enum ZoneStatusAlt {           NEUTRAL, HOSTILE, CONTROLLED;                       }

/***
 *    ██╗   ██╗████████╗██╗██╗     ███████╗
 *    ██║   ██║╚══██╔══╝██║██║     ██╔════╝
 *    ██║   ██║   ██║   ██║██║     ███████╗
 *    ██║   ██║   ██║   ██║██║     ╚════██║
 *    ╚██████╔╝   ██║   ██║███████╗███████║
 *     ╚═════╝    ╚═╝   ╚═╝╚══════╝╚══════╝
 *
 */
class UtilsAlt {

    /***
     *    ███████╗ ██████╗ ███╗   ██╗███████╗
     *    ╚══███╔╝██╔═══██╗████╗  ██║██╔════╝
     *      ███╔╝ ██║   ██║██╔██╗ ██║█████╗
     *     ███╔╝  ██║   ██║██║╚██╗██║██╔══╝
     *    ███████╗╚██████╔╝██║ ╚████║███████╗
     *    ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝
     *
     */

    public static boolean hasNeighboorJustBeenTaken(ZoneAlt zoneAlt) {
        for (ZoneAlt z : zoneAlt.adjacentZoneAlts)
            if (z.justBeenTaken)
                return true;
        return false;
    }

    public static boolean hasEnemies(ZoneAlt zoneAlt) {
        for (int i = 0; i < zoneAlt.drones.length; i++)
            if (zoneAlt.drones[i] > 0 && i != PlayerV3.myId)
                return true;
        return false;
    }

    public static int getNbEnemieZonesNearby(ZoneAlt zoneAlt) {
        int i = 0;
        for (ZoneAlt z : zoneAlt.adjacentZoneAlts)
            if (!UtilsAlt.isMine(z) && !UtilsAlt.isFree(z))
                i++;
        return i;
    }

    public static ZoneStatusAlt determineStatus(ZoneAlt zoneAlt) {
        if (PlayerV3.myId == zoneAlt.ownerId)        zoneAlt.status = ZoneStatusAlt.CONTROLLED;
        else if (zoneAlt.ownerId == -1)            zoneAlt.status = ZoneStatusAlt.NEUTRAL;
        else                                    zoneAlt.status = ZoneStatusAlt.HOSTILE;
        return zoneAlt.status;
    }

    public static boolean isBorder(ZoneAlt zoneAlt) {
        for (ZoneAlt z : zoneAlt.adjacentZoneAlts)
            if (!UtilsAlt.isFree(z) && !UtilsAlt.isMine(z))
                return true;
        return false;
    }

    public static boolean needToMove(ZoneAlt zoneAlt) {    return UtilsAlt.isMine(zoneAlt) && UtilsAlt.hasDrones(zoneAlt);            }
    public static boolean hasDrones(ZoneAlt zoneAlt) {     return zoneAlt.drones[PlayerV3.myId] > 0;                             }
    public static boolean isMine(ZoneAlt zoneAlt) {        return zoneAlt.status == ZoneStatusAlt.CONTROLLED;                   }
    public static boolean isFree(ZoneAlt zoneAlt) {        return zoneAlt.status == ZoneStatusAlt.NEUTRAL;                      }

    public static boolean hasEnemiesNearby(ZoneAlt zoneAlt) {
        for (ZoneAlt z : zoneAlt.adjacentZoneAlts)
            if (UtilsAlt.hasEnemies(z))
                return true;
        return false;
    }

    public static boolean hasDrones(List<ZoneAlt> zoneAlts) {
        for (ZoneAlt z : zoneAlts)
            if (UtilsAlt.hasDrones(z))
                return true;
        return false;
    }
    /***
     *     ██████╗ ██████╗ ███╗   ██╗████████╗██╗███╗   ██╗███████╗███╗   ██╗████████╗███████╗
     *    ██╔════╝██╔═══██╗████╗  ██║╚══██╔══╝██║████╗  ██║██╔════╝████╗  ██║╚══██╔══╝██╔════╝
     *    ██║     ██║   ██║██╔██╗ ██║   ██║   ██║██╔██╗ ██║█████╗  ██╔██╗ ██║   ██║   ███████╗
     *    ██║     ██║   ██║██║╚██╗██║   ██║   ██║██║╚██╗██║██╔══╝  ██║╚██╗██║   ██║   ╚════██║
     *    ╚██████╗╚██████╔╝██║ ╚████║   ██║   ██║██║ ╚████║███████╗██║ ╚████║   ██║   ███████║
     *     ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝   ╚═╝   ╚═╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚══════╝
     *
     */


    public static void zoneStatusChanged(ContinentAlt continentAlt, ZoneStatusAlt previousStatus, ZoneStatusAlt newStatus, ZoneAlt zoneAlt) {
        UtilsAlt.getMap(continentAlt, previousStatus).remove(zoneAlt.id);
        UtilsAlt.getMap(continentAlt, newStatus).put(zoneAlt.id, zoneAlt);
    }

    private static Map<Integer, ZoneAlt> getMap(ContinentAlt continentAlt, ZoneStatusAlt status) {
        switch (status) {
            case CONTROLLED:    return continentAlt.controlledZones;
            case HOSTILE:       return continentAlt.hostileZones;
            default:            return continentAlt.neutralZones;
        }
    }

    public static ContinentStatusAlt determineStatus(ContinentAlt continentAlt) {
        if (continentAlt.controlledZones.size() == continentAlt.zones.size())
            continentAlt.status = ContinentStatusAlt.PACIFIED;
        else if (continentAlt.hostileZones.size() == continentAlt.zones.size())
            continentAlt.status = ContinentStatusAlt.HOSTILE;
        else continentAlt.status = ContinentStatusAlt.DISPUTED;
        return continentAlt.status;
    }

    public static boolean hasNeutralZones(ContinentAlt continentAlt) {
        return continentAlt.neutralZones.values().size() > 0;
    }

    public static boolean hasZoneOnContinent(ContinentAlt continentAlt) {
        return !(continentAlt.neutralZones.size() == continentAlt.zones.size());
    }

    static boolean lessDrones(int[] totalDrones) {
        for (int i = 0; i < 4; i++)
            if (totalDrones[i] >= totalDrones[PlayerV3.myId] && i != PlayerV3.myId)
                return true;
        return false;
    }

    public static int[] countDrones(ContinentAlt continentAlt) {
        int[] totalDrones = new int[4];
        for (ZoneAlt z : continentAlt.zones.values())
            for (int i = 0; i < 4; i++)
                totalDrones[i] += z.drones[i];
        return totalDrones;
    }

    public static Collection<? extends ZoneAlt> getNeutralZoneWithPlatinium(ContinentAlt continentAlt) {
        List<ZoneAlt> zoneAlts = new ArrayList<>();
        for (ZoneAlt z : continentAlt.neutralZones.values())
            if (z.platinium > 0)
                zoneAlts.add(z);
        return zoneAlts;
    }

    public static Collection<? extends ZoneAlt> getNeutralZoneBorder(ContinentAlt continentAlt) {
        List<ZoneAlt> zoneAlts = new ArrayList<>();
        for (ZoneAlt z : continentAlt.neutralZones.values())
            if (UtilsAlt.isBorder(z))
                zoneAlts.add(z);
        return zoneAlts;
    }

    public static ZoneAlt getCorner(ContinentAlt continentAlt, int i) {
        for (ZoneAlt z : continentAlt.zones.values())
            if (z.adjacentZoneAlts.size() == i)
                return z;
        return null;
    }

    public static boolean iHaveNoDrones(ContinentAlt continentAlt, int[] drones) {
        return drones[PlayerV3.myId] == 0;
    }

    /***
     *    ██╗    ██╗ ██████╗ ██████╗ ██╗     ██████╗
     *    ██║    ██║██╔═══██╗██╔══██╗██║     ██╔══██╗
     *    ██║ █╗ ██║██║   ██║██████╔╝██║     ██║  ██║
     *    ██║███╗██║██║   ██║██╔══██╗██║     ██║  ██║
     *    ╚███╔███╔╝╚██████╔╝██║  ██║███████╗██████╔╝
     *     ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═════╝
     *
     */

    private static boolean txt = true;
    public static void executeCommands(List<? extends Object> commands) {
        if (commands.size() > 0) {
            StringBuilder commandsOutput = new StringBuilder();
            for (Object c : commands)
                commandsOutput.append(c.toString());
            if (txt) {
                if (PlayerV3.R.nextInt(10) == 5)
                    commandsOutput.append("#All your base are belong to us");
            }
            txt = !txt;
            System.out.println(commandsOutput.toString());
        } else
            System.out.println("WAIT");
    }

    public static ContinentAlt getContinentWithMoreRessources(List<ContinentAlt> continentAlts) {
        int best = 0;
        ContinentAlt continentAlt = null;
        for (ContinentAlt c : continentAlts) {
            if (c.ressources > best) {
                continentAlt = c;
                best = c.ressources;
            }
        }
        return continentAlt;
    }

    public static int getOtherPlayerActive(int[] drones) {
        int players = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == PlayerV3.myId)
                continue;
            if (drones[i] > 0)
                players++;
        }
        return players;
    }

    public static int getEnemieDrones(int[] drones) {
        int cpt = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == PlayerV3.myId)
                continue;
            if (drones[i] > cpt)
                cpt = drones[i];
        }
        return cpt;
    }
}