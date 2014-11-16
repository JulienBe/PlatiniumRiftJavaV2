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
class Player {

    static final float FIRST_TURN_DIVISION = 2;
    // Je veux du java 8 bordel !
    private static long longestUpdate = 0, longestSpawn = 0, longestMvt = 0;
    public static final int DRONE_COST = 20;
    public static int playerCount, myId;
    public static final Random R = new Random();
    public static boolean firstTurn = true;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        final World world = init(in);

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
            world.update(in);
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
            world.movements();
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
            world.spawnDrones(platinum);
            long spawn = System.currentTimeMillis() - begin;
            System.err.println("spawn : " + spawn + "/" + longestSpawn);
            if (spawn > longestSpawn)
                longestSpawn = spawn;
            System.err.println("Spawn : " + (System.currentTimeMillis() - begin));
            firstTurn = false;
        }
    }

    private static World init(Scanner in) {
        playerCount = in.nextInt(); // the amount of players (2 to 4)
        myId = in.nextInt(); // my player ID (0, 1, 2 or 3)
        int zoneCount = in.nextInt(); // the amount of zones on the map
        int linkCount = in.nextInt(); // the amount of links between all
        in.nextLine();
        World world = new World();
        world.init(in, zoneCount, linkCount);
        return world;
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
class World {

    // 0.85 : 52/772 -- 40.19
    private static final float SPAWN_ATTRACTION_DIMINISHING = 0.55f;
    private int zoneCount;
    private final List<Continent> continents = new ArrayList<>(), disputed = new ArrayList<>();

    void init(Scanner in, int zoneCount, int linkCount) {
        Map<Integer, Zone> zones = new HashMap<>();
        this.zoneCount = zoneCount;
        for (int i = 0; i < zoneCount; i++) {
            int zoneId = in.nextInt();
            int platinumSource = in.nextInt();
            Zone zone = new Zone(platinumSource, zoneId);
            zones.put(zoneId, zone);
            in.nextLine();
        }

        for (int i = 0; i < linkCount; i++) {
            int zone1 = in.nextInt();
            int zone2 = in.nextInt();
            Zone z1 = zones.get(zone1);
            Zone z2 = zones.get(zone2);
            z1.addAdjacentZone(z2);
            z2.addAdjacentZone(z1);
            in.nextLine();
        }

        for (Zone z : zones.values()) {
            Continent continent = getRattachedContinent(z);
            if (continent ==  null) {
                continent = getPossibleContinent(z);
                if (continent == null) {
                    continent = new Continent();
                    continents.add(continent);
                }
                continent.addZone(z);
            }
        }
        for (Continent c : continents)
            c.initFinished();
        disputed.addAll(continents);
    }

    private Continent getPossibleContinent(Zone zone) {
        for (Zone z : zone.getAllAdjacentZones()) {
            Continent c = getRattachedContinent(z);
            if (c != null)
                return c;
        }
        return null;
    }

    private Continent getRattachedContinent(Zone zone) {
        for (Continent c : continents)
            if (c.zones.containsValue(zone))
                return c;
        return null;
    }

    private void addContinentToList(Continent continent) {
        if (continent.status == ContinentStatus.DISPUTED)   disputed.add(continent);
        else                                                disputed.remove(continent);
    }

    void update(Scanner in) {
        for (Continent c : continents)
            c.newTurn();
        for (int i = 0; i < zoneCount; i++)
            updateZone(in);

        disputed.clear();

        for (Continent c : continents) {
            Utils.determineStatus(c);
            addContinentToList(c);
            c.updateFinished();
        }
    }

    private void updateZone(Scanner in) {
        int zId = in.nextInt(); // this zone's ID
        int ownerId = in.nextInt(); // the player who owns this zone (-1 otherwise)
        int podsP0 = in.nextInt(); // player 0's PODs on this zone
        int podsP1 = in.nextInt(); // player 1's PODs on this zone
        int podsP2 = in.nextInt(); // player 2's PODs on this zone (always 0 for a two player game)
        int podsP3 = in.nextInt(); // player 3's PODs on this zone (always 0 for a two or three player game)
        in.nextLine();

        for (Continent c : continents)
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
        List<CommandMvt> commands = new ArrayList<>();

        List<Zone> zonesWithDrones = new ArrayList<>();
        for (Continent c : disputed)
            zonesWithDrones.addAll(c.zoneWithDrones);
        Iterator<Zone> it = zonesWithDrones.iterator();
        while (it.hasNext()) {
            Zone origin = it.next();
            int pop = 0;
            int i = origin.podsToKeep();
            for (; i < origin.getDrones(); i++) {
                Zone zoneToGo = origin.getAdjacenToGoTo();
                if (zoneToGo != null) {
                    pop++;
                    sendDrone(commands, origin, zoneToGo);
                    System.err.println("Adjacent : " + origin.id + " -> " + zoneToGo.id + " with future drones : " + zoneToGo.futurDrones);
                }
            }
            if (origin.getDrones() == pop)
                it.remove();
        }
        Iterator<Zone> itDistant = zonesWithDrones.iterator();
        while (itDistant.hasNext()) {
            Zone origin = itDistant.next();
            int i = origin.podsToKeep();
            for (; i < origin.getDrones(); i++) {
                MagnetismResolver magnetismResolver = origin.getDistantToGoTo();
                if (magnetismResolver != null) {
                    sendDrone(commands, origin, magnetismResolver.adjacent);
                    System.err.println("Distant : " + origin.id + " -> " + magnetismResolver.adjacent.id + "    TARGET : " + magnetismResolver.target.id);
                }
            }
        }
        Utils.executeCommands(commands);
    }

    private void sendDrone(List<CommandMvt> commands, Zone z, Zone zoneToGo) {
        CommandMvt commandMvt = new CommandMvt();
        commandMvt.from = z.id;
        commandMvt.to = zoneToGo.id;
        commandMvt.drones = 1;
        commands.add(commandMvt);
        zoneToGo.futurDrones++;
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
        List<CommandSpawn> commands = new ArrayList<>();

        TreeSet<ContinentSpawnAnalytic> spawnAnalytics = new TreeSet<>();
        for (Continent c : disputed)
            spawnAnalytics.add(c.getSpawnAnalytics());

        if (spawnAnalytics.isEmpty())
            return;
        while (platinum >= Player.DRONE_COST)
            platinum = spawnDrone(platinum, commands, spawnAnalytics);

        Utils.executeCommands(commands);
    }

    private int spawnDrone(int platinum, List<CommandSpawn> commands, TreeSet<ContinentSpawnAnalytic> spawnAnalytics) {
        ContinentSpawnAnalytic spawnAnalytic = spawnAnalytics.pollFirst();
        SpawnResolver candidate = spawnAnalytic.pollBest();

        CommandSpawn commandSpawn = new CommandSpawn();
        commandSpawn.drones = 1;
//        if (Player.firstTurn)
//            commandSpawn.drones++;
        commandSpawn.to = candidate.zone.id;

        candidate.magnetism *= SPAWN_ATTRACTION_DIMINISHING;
        spawnAnalytic.continent.futurDrones += commandSpawn.drones;
        if (spawnAnalytic.continent.futurDrones == 1)
            spawnAnalytic.totalValue /= Continent.NO_DRONES_SPAWN_MULTI;
        commands.add(commandSpawn);
        spawnAnalytic.addCandidate(candidate);
        spawnAnalytics.add(spawnAnalytic);
        return platinum - Player.DRONE_COST;
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

class Continent {

    static final float NO_DRONES_SPAWN_MULTI = 1;
    int ressources = 0, futurDrones = 0, otherPlayersActive;
    int[] drones = new int[4];
    ContinentStatus status = ContinentStatus.DISPUTED;
    Map<Integer, Zone> zones = new HashMap<>(), neutralZones = new HashMap<>(), hostileZones = new HashMap<>(), controlledZones = new HashMap<>();
    List<Zone> zoneWithDrones = new ArrayList<>();
    int group = 0;

    void addZone(Zone z) {
        zones.put(z.id, z);
        neutralZones.put(z.id, z);
        ressources += z.platinium;
        z.continent = this;
    }

    public void initFinished() {
        for (Zone z : zones.values())
            z.initFinished();
    }

    public void newTurn() {
        for (int i = 0; i < drones.length; i++)
            drones[i] += 0;
        futurDrones = 0;
        zoneWithDrones.clear();
        group = 0;
    }
    /**
     * @return true if the zone has been updated
     */
    boolean update(int zId, int ownerId, int podsP0, int podsP1, int podsP2, int podsP3) {
        if (zones.containsKey(zId)) {
            Zone zone = zones.get(zId);
            ZoneStatus previousStatus = zone.status;
            ZoneStatus newStatus = zone.update(ownerId, podsP0, podsP1, podsP2, podsP3);
            if (previousStatus != newStatus)
                Utils.zoneStatusChanged(this, previousStatus, newStatus, zone);
            drones[0] += podsP0;
            drones[1] += podsP1;
            drones[2] += podsP2;
            drones[3] += podsP3;
            if (drones[Player.myId] > 0)
                zoneWithDrones.add(zone);
            return true;
        }
        return false;
    }

    void updateFinished() {
        otherPlayersActive = Utils.getOtherPlayerActive(drones);
        for (Zone z : zones.values())
            z.updateFinished();

    }

    /***
     *      ___
     *     / __|  _ __   __ _  __ __ __  _ _
     *     \__ \ | '_ \ / _` | \ V  V / | ' \
     *     |___/ | .__/ \__,_|  \_/\_/  |_||_|
     *           |_|
     */


    public ContinentSpawnAnalytic getSpawnAnalytics() {
        ContinentSpawnAnalytic spawnAnalytic = new ContinentSpawnAnalytic(this);
        for (Zone z : neutralZones.values())
            spawnAnalytic.addCandidate(getSpawnResolverFreeZones(z));
        for (Zone z : controlledZones.values())
            spawnAnalytic.addCandidate(getSpawnResolverMyZones(z));
        if (drones[Player.myId] == 0)
            spawnAnalytic.totalValue *= NO_DRONES_SPAWN_MULTI;
        return spawnAnalytic;
    }

    private SpawnResolver getSpawnResolverFreeZones(Zone z) {
        z.spawnResolver.magnetism = z.evaluateFreeZone(otherPlayersActive);
        return z.spawnResolver;
    }
    private SpawnResolver getSpawnResolverMyZones(Zone z) {
        z.spawnResolver.magnetism = z.evaluateMyZone(otherPlayersActive);
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

class Zone {

    // SPAWN
    private static final float
            MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE = 12,
    //*2 => 52ème / 40.30
    ADJACENT_ZONE_SIZE_DIV = 2;
    // MVT
    private static final int MAX_DISTANCE = 7, MAX_DRONES = 5;


    ZoneStatus status = ZoneStatus.NEUTRAL;
    // /!\
    SpawnResolver spawnResolver = new SpawnResolver(this);
    final int id;
    int platinium, ownerId = -1, platiniumNearby = 0, nbEnemies, futurDrones = 0;
    int[] drones = new int[4], adjacentDrones = new int[4];
    List<Zone> adjacentZones = new ArrayList<>(), adjacentWithRessources = new ArrayList<>(), adjacentOfAdjacentWithRessources = new ArrayList<>();
    boolean justBeenTaken = false;
    public Continent continent;

    Zone(int platinium, int id) {
        this.platinium = platinium;
        this.id = id;
    }

    void addAdjacentZone(Zone zone) {
        adjacentZones.add(zone);
        if (zone.platinium > 0) {
            adjacentWithRessources.add(zone);
            platiniumNearby += zone.platinium;
        }
    }

    public void initFinished() {
        for (Zone z : adjacentZones) {
            for (Zone distant : z.adjacentWithRessources) {
                if (!distant.equals(this))
                    adjacentOfAdjacentWithRessources.add(distant);
            }
        }
    }

    ZoneStatus update(int ownerId, int podsP0, int podsP1, int podsP2, int podsP3) {
        justBeenTaken = this.ownerId == Player.myId && ownerId != this.ownerId;
        this.ownerId = ownerId;
        drones[0] = podsP0;
        drones[1] = podsP1;
        drones[2] = podsP2;
        drones[3] = podsP3;
        futurDrones = 0;
        nbEnemies = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            nbEnemies += drones[i];
        }
        return Utils.determineStatus(this);
    }

    int getDrones() {
        return drones[Player.myId];
    }

    List<Zone> getAllAdjacentZones() {
        List<Zone> zones = new ArrayList<>();
        return createContinent(zones);
    }

    private List<Zone> createContinent(List<Zone> zones) {
        zones.add(this);
        for (Zone z : adjacentZones)
            if (!zones.contains(z))
                z.createContinent(zones);
        return zones;
    }

    public void updateFinished() {
        for (int i = 0; i < adjacentDrones.length; i++)
            adjacentDrones[i] = 0;
        for (Zone z : adjacentZones)
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
    public Zone getAdjacenToGoTo() {
        Zone zone = null;
        if (platinium > 0) {
            if (Utils.hasEnemiesNearby(this) && futurDrones < Utils.getNbEnemyDronesNearby(this) + 2)
                return this;
        }
        if (Utils.getOtherPlayerActive(adjacentDrones) > 1)
            return null;

        int best = 0;
        for (Zone z : adjacentWithRessources) {
            if (z.futurDrones != 0 || Utils.isMine(z) || (Utils.hasEnemies(z)))
                continue;
            if (zone == null) {
                zone = z;
                best = z.platinium;
            } else if (best < z.platinium) {
                zone = z;
                best = z.platinium;
            }
        }
    /*        if (zone == null) {
                for (Zone z : adjacentZones)
                    if (!Utils.isMine(z) && !Utils.hasEnemies(z) && z.futurDrones == 0)
                        return z;
            }*/

        return zone;
    }

    public MagnetismResolver getDistantToGoTo() {
        if (platinium > 0)
            if (Utils.hasEnemiesNearby(this) && futurDrones < Utils.getNbEnemyDronesNearby(this) + 2)
                return null;
        TreeSet<MagnetismResolver> candidates = new TreeSet<>();
        for (Zone z : adjacentZones)
            z.examineZone(getDrones(), candidates, 0, z, id);
        if (candidates.size() > 0)
            return candidates.first();
        int max = 0;
        MagnetismResolver magnetismResolver = new MagnetismResolver();
        for (Zone z : adjacentZones)
            if (z.adjacentZones.size() > max) {
                magnetismResolver.adjacent = z;
                magnetismResolver.target = z;
                max = z.adjacentZones.size();
            }
        if (magnetismResolver.adjacent != null)
            return magnetismResolver;
        return null;
    }

    private void examineZone(int drones, TreeSet<MagnetismResolver> candidates, float totalMagnetism, Zone adjacent, int... ids) {
        for (int i : ids)
            if (i == id)
                return;
        if (Utils.getEnemieDrones(this.drones) >= drones)
            return;
        float magnetism = getMagnetism();
        int[] newIds = new int[ids.length + 1];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        newIds[newIds.length - 1] = id;
        if (magnetism > 0) {
            magnetism /= newIds.length * 2;
            totalMagnetism += magnetism;
            candidates.add(new MagnetismResolver(totalMagnetism, adjacent, this));
        }

        if (newIds.length >= MAX_DISTANCE)
            return;

        for (Zone z : adjacentZones)
            z.examineZone(drones, candidates, totalMagnetism, adjacent, newIds);
    }

    private int getMagnetism() {
        if (Utils.getOtherPlayerActive(adjacentDrones) > 1)
            return -1;
        int i = -futurDrones;
        if (Utils.isMine(this)) {
            if (Utils.isBorder(this))
                i++;
            return i;
        }
        if (!Utils.hasEnemies(this))
            i += 5 + platinium * 5;
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

    public int evaluateFreeZone(int otherPlayerActive) {

        if (Utils.allAdjacentAreMine(this) && continent.drones[Player.myId] > 30)
            return -1;
        int value = 0;
        if (continent.futurDrones == 0)
            value += 10;
        for (Zone z : adjacentWithRessources) {
            if (!Utils.hasEnemies(z) && !Utils.isMine(z)) {
                value += (z.platinium * 2) / (1 + z.futurDrones);
                if (!Utils.hasEnemiesNearby(z))
                    value += (z.platinium * 2) / (1 + z.futurDrones);
            }
        }

        for (Zone z : adjacentOfAdjacentWithRessources)
            if (!Utils.isMine(z) && !Utils.hasEnemies(z))
                value += (z.platinium) / (1 + z.futurDrones);

        if (futurDrones == 0 && Utils.isFree(this))
            value += platinium * MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE;


        value *= 7 - adjacentZones.size();
        if (Utils.isMine(this) && Utils.hasEnemiesNearby(this))
            value *= (platinium*2) + 1;
        else
            value /= Utils.getNbEnemieZonesNearby(this) + 1;
        value /= futurDrones*2 + 1;
        if (continent.drones[Player.myId] == 0)
            value *= 2;
        return value;
//
//        int value = 0;
//
//        for (Zone z : adjacentWithRessources) {
//            if (!Utils.hasEnemies(z) && !Utils.isMine(z)) {
//                value += (z.platinium * 2) / (1 + z.futurDrones);
//                if (!Utils.hasEnemiesNearby(z))
//                    value += (z.platinium * 2) / (1 + z.futurDrones);
//            }
//        }
//
//        for (Zone z : adjacentOfAdjacentWithRessources)
//            if (!Utils.isMine(z) && !Utils.hasEnemies(z))
//                value += (z.platinium * 2) / (1 + z.futurDrones);
//
//        value += platinium * MULTI_PT_IF_FREE_N_NO_FUTURE_DRONE;
//        value /= futurDrones + 1;
//        // Ca ça fait gagner plein de places
//        value *= 7 - adjacentZones.size();
//
//        return value;
    }



    public int evaluateMyZone(int otherPlayerActive) {
        return evaluateFreeZone(otherPlayerActive);
//        int value = 0;
//        if (Utils.isBorder(this))
//            value += 2;
//        for (Zone z : adjacentWithRessources)
//            if (Utils.isFree(z) && !Utils.hasEnemiesNearby(z))
//                value += (z.platinium * 4) / (1 + z.futurDrones);
//
//        for (Zone z : adjacentOfAdjacentWithRessources)
//            if (!Utils.isMine(z) && !Utils.hasEnemies(z))
//                value += (z.platinium) / ( 1 + z.futurDrones);
//
//        value /= futurDrones + 1;
//
//        if (Utils.hasEnemiesNearby(this))
//            value *= 1 + platinium * 2;
//
//        return value;
    }

    public boolean shouldDefend() {
        return platinium > 0 && Utils.hasEnemiesNearby(this);
    }

    public int podsToKeep() {
        if (platinium == 0)
            return 0;
        int spare = getDrones() - Utils.getNbEnemieZonesNearby(this);
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

class CommandMvt {
    int drones, from, to;
    @Override    public String toString() {        return drones + " " + from + " " + to + " ";    }
}
class CommandSpawn {
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

class MagnetismResolver implements Comparable<MagnetismResolver>{
    float magnetism;
    Zone adjacent, target;

    public MagnetismResolver(float magnetism, Zone adjacent, Zone target) {
        this.magnetism = magnetism;
        this.adjacent = adjacent;
        this.target = target;
    }

    public MagnetismResolver() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MagnetismResolver that = (MagnetismResolver) o;

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
    public int compareTo(MagnetismResolver o) {
        return (int) ((o.magnetism * 1000) - (magnetism * 1000));
    }
}

class ContinentSpawnAnalytic  implements Comparable<ContinentSpawnAnalytic> {
    TreeSet<SpawnResolver> candidates = new TreeSet<>();
    int totalValue;
    final Continent continent;

    public ContinentSpawnAnalytic(Continent continent) {
        this.continent = continent;
    }

    public void addCandidate(SpawnResolver spawnResolver) {
        totalValue += spawnResolver.magnetism;
        candidates.add(spawnResolver);
    }

    public SpawnResolver pollBest() {
        SpawnResolver spawnResolver = candidates.pollFirst();
        totalValue -= spawnResolver.magnetism;
        return spawnResolver;
    }

    @Override
    public int compareTo(ContinentSpawnAnalytic o) {
        return o.totalValue * 1000 - totalValue * 1000;
    }

}

class SpawnResolver implements Comparable<SpawnResolver> {
    int magnetism;
    final Zone zone;

    public SpawnResolver(Zone zone) {
        this.zone = zone;
    }

    void reset() {
        magnetism = -500;
    }

    @Override
    public int compareTo(SpawnResolver o) {
        return o.magnetism - magnetism;
    }
}


enum ContinentStatus {      PACIFIED, HOSTILE, DISPUTED;                        }
enum ZoneStatus {           NEUTRAL, HOSTILE, CONTROLLED;                       }

/***
 *    ██╗   ██╗████████╗██╗██╗     ███████╗
 *    ██║   ██║╚══██╔══╝██║██║     ██╔════╝
 *    ██║   ██║   ██║   ██║██║     ███████╗
 *    ██║   ██║   ██║   ██║██║     ╚════██║
 *    ╚██████╔╝   ██║   ██║███████╗███████║
 *     ╚═════╝    ╚═╝   ╚═╝╚══════╝╚══════╝
 *
 */
class Utils {

    /***
     *    ███████╗ ██████╗ ███╗   ██╗███████╗
     *    ╚══███╔╝██╔═══██╗████╗  ██║██╔════╝
     *      ███╔╝ ██║   ██║██╔██╗ ██║█████╗
     *     ███╔╝  ██║   ██║██║╚██╗██║██╔══╝
     *    ███████╗╚██████╔╝██║ ╚████║███████╗
     *    ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝
     *
     */

    public static boolean hasNeighboorJustBeenTaken(Zone zone) {
        for (Zone z : zone.adjacentZones)
            if (z.justBeenTaken)
                return true;
        return false;
    }

    public static boolean hasEnemies(Zone zone) {
        for (int i = 0; i < zone.drones.length; i++)
            if (zone.drones[i] > 0 && i != Player.myId)
                return true;
        return false;
    }

    public static int getNbEnemieZonesNearby(Zone zone) {
        int i = 0;
        for (Zone z : zone.adjacentZones)
            if (!Utils.isMine(z) && !Utils.isFree(z))
                i++;
        return i;
    }

    public static ZoneStatus determineStatus(Zone zone) {
        if (Player.myId == zone.ownerId)        zone.status = ZoneStatus.CONTROLLED;
        else if (zone.ownerId == -1)            zone.status = ZoneStatus.NEUTRAL;
        else                                    zone.status = ZoneStatus.HOSTILE;
        return zone.status;
    }

    public static boolean isBorder(Zone zone) {
        for (Zone z : zone.adjacentZones)
            if (!Utils.isFree(z) && !Utils.isMine(z))
                return true;
        return false;
    }

    public static boolean needToMove(Zone zone) {    return Utils.isMine(zone) && Utils.hasDrones(zone);            }
    public static boolean hasDrones(Zone zone) {     return zone.drones[Player.myId] > 0;                             }
    public static boolean isMine(Zone zone) {        return zone.status == ZoneStatus.CONTROLLED;                   }
    public static boolean isFree(Zone zone) {        return zone.status == ZoneStatus.NEUTRAL;                      }

    public static boolean hasEnemiesNearby(Zone zone) {
        for (Zone z : zone.adjacentZones)
            if (Utils.hasEnemies(z))
                return true;
        return false;
    }

    public static boolean hasDrones(List<Zone> zones) {
        for (Zone z : zones)
            if (Utils.hasDrones(z))
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


    public static void zoneStatusChanged(Continent continent, ZoneStatus previousStatus, ZoneStatus newStatus, Zone zone) {
        Utils.getMap(continent, previousStatus).remove(zone.id);
        Utils.getMap(continent, newStatus).put(zone.id, zone);
    }

    private static Map<Integer, Zone> getMap(Continent continent, ZoneStatus status) {
        switch (status) {
            case CONTROLLED:    return continent.controlledZones;
            case HOSTILE:       return continent.hostileZones;
            default:            return continent.neutralZones;
        }
    }

    public static ContinentStatus determineStatus(Continent continent) {
        if (continent.controlledZones.size() == continent.zones.size())
            continent.status = ContinentStatus.PACIFIED;
        else if (continent.hostileZones.size() == continent.zones.size())
            continent.status = ContinentStatus.HOSTILE;
        else continent.status = ContinentStatus.DISPUTED;
        return continent.status;
    }

    public static boolean hasNeutralZones(Continent continent) {
        return continent.neutralZones.values().size() > 0;
    }

    public static boolean hasZoneOnContinent(Continent continent) {
        return !(continent.neutralZones.size() == continent.zones.size());
    }

    public static boolean hasLessDronesThanEnemies(Continent continent) {
        int[] totalDrones = Utils.countDrones(continent);
        return Utils.lessDrones(totalDrones);
    }

    private static boolean lessDrones(int[] totalDrones) {
        for (int i = 0; i < 4; i++)
            if (totalDrones[i] >= totalDrones[Player.myId] && i != Player.myId)
                return true;
        return false;
    }

    public static int[] countDrones(Continent continent) {
        int[] totalDrones = new int[4];
        for (Zone z : continent.zones.values())
            for (int i = 0; i < 4; i++)
                totalDrones[i] += z.drones[i];
        return totalDrones;
    }

    public static Collection<? extends Zone> getNeutralZoneWithPlatinium(Continent continent) {
        List<Zone> zones = new ArrayList<>();
        for (Zone z : continent.neutralZones.values())
            if (z.platinium > 0)
                zones.add(z);
        return zones;
    }

    public static Collection<? extends Zone> getNeutralZoneBorder(Continent continent) {
        List<Zone> zones = new ArrayList<>();
        for (Zone z : continent.neutralZones.values())
            if (Utils.isBorder(z))
                zones.add(z);
        return zones;
    }

    public static Zone getCorner(Continent continent, int i) {
        for (Zone z : continent.zones.values())
            if (z.adjacentZones.size() == i)
                return z;
        return null;
    }

    public static boolean iHaveNoDrones(Continent continent, int[] drones) {
        return drones[Player.myId] == 0;
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
                if (Player.R.nextInt(10) == 5)
                    commandsOutput.append("#All your base are belong to us");
            }
            txt = !txt;
            System.out.println(commandsOutput.toString());
        } else
            System.out.println("WAIT");
    }

    public static Continent getContinentWithMoreRessources(List<Continent> continents) {
        int best = 0;
        Continent continent = null;
        for (Continent c : continents) {
            if (c.ressources > best) {
                continent = c;
                best = c.ressources;
            }
        }
        return continent;
    }

    public static int getOtherPlayerActive(int[] drones) {
        int players = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            if (drones[i] > 0)
                players++;
        }
        return players;
    }

    public static int getEnemieDrones(int[] drones) {
        int cpt = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            if (drones[i] > cpt)
                cpt = drones[i];
        }
        return cpt;
    }

    public static int getNbEnemyDronesNearby(Zone zone) {
        int cpt = 0;
        for (Zone z : zone.adjacentZones) {
            if (z.ownerId == -1)
                continue;
            cpt += z.drones[z.ownerId];
        }
        return cpt;
    }

    public static boolean allAdjacentAreMine(Zone zone) {
        for (Zone z : zone.adjacentZones)
            if (!isMine(z))
                return false;
        return true;
    }
}
