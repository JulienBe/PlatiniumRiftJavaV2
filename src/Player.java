import java.util.*;

/***
 *    ██████╗ ██╗      █████╗ ██╗   ██╗███████╗██████╗
 *    ██╔══██╗██║     ██╔══██╗╚██╗ ██╔╝██╔════╝██╔══██╗
 *    ██████╔╝██║     ███████║ ╚████╔╝ █████╗  ██████╔╝
 *    ██╔═══╝ ██║     ██╔══██║  ╚██╔╝  ██╔══╝  ██╔══██╗
 *    ██║     ███████╗██║  ██║   ██║   ███████╗██║  ██║
 *    ╚═╝     ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═╝  ╚═╝
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
//            if (firstTurn && playerCount > 2)
//                platinum *= 0.5f;
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
 */
class World {

    // 0.85 : 52/772 -- 40.19
    private static final float SPAWN_ATTRACTION_DIMINISHING = 0.55f;
    private int zoneCount;
    private final List<Continent> continents = new ArrayList<>(), disputed = new ArrayList<>();

    void init(Scanner in, int zoneCount, int linkCount) {
        Map<Integer, Zone> zones = zoneInit(in, zoneCount);

        linkInit(in, linkCount, zones);

        createContinent(zones);

        for (Continent c : continents)
            c.initFinished();
        disputed.addAll(continents);
    }

    private void createContinent(Map<Integer, Zone> zones) {
        for (Zone z : zones.values()) {
            Continent continent = getContinentOfAdjacent(z);
            if (continent == null) {
                continent = new Continent();
                continents.add(continent);
            }
            continent.addZone(z);
        }
    }

    private Continent getRattachedContinent(Zone zone) {
        for (Continent c : continents)
            if (c.zones.containsValue(zone))
                return c;
        return null;
    }

    private Continent getContinentOfAdjacent(Zone zone) {
        for (Zone z : zone.getAllAdjacentZones()) {
            Continent c = getRattachedContinent(z);
            if (c != null)
                return c;
        }
        return null;
    }

    private void linkInit(Scanner in, int linkCount, Map<Integer, Zone> zones) {
        for (int i = 0; i < linkCount; i++) {
            Zone z1 = zones.get(in.nextInt());
            Zone z2 = zones.get(in.nextInt());
            z1.addAdjacentZone(z2);
            z2.addAdjacentZone(z1);
            in.nextLine();
        }
    }

    private Map<Integer, Zone> zoneInit(Scanner in, int zoneCount) {
        Map<Integer, Zone> zones = new HashMap<>();
        this.zoneCount = zoneCount;
        for (int i = 0; i < zoneCount; i++) {
            int id = in.nextInt();
            zones.put(id, new Zone(id, in.nextInt()));
            in.nextLine();
        }
        return zones;
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
     */
    void movements() {
        List<CommandMvt> commands = new ArrayList<>();
        /**
         * REINFORCEMENT
         */
        reinforcement(commands);

        List<Zone> zonesWithDrones = new ArrayList<>();
        for (Continent c : disputed)
            zonesWithDrones.addAll(c.zoneWithDrones);
        /**
         * ADJACENT
         */
        mvtAdjacentOpti(commands, zonesWithDrones);
        /**
         * DISTANT
         */
        mvtDistant(commands, zonesWithDrones);
        Utils.executeCommands(commands);
    }


    private void reinforcement(List<CommandMvt> commands) {
        for (Continent c : disputed) {
            TreeSet<Zone> zoneToReinforce = new TreeSet<>();
            for (Zone z : c.zoneWithRessources) {
                if (!Utils.isMine(z))
                    continue;
                zoneToReinforce.add(z);
            }
            mvtDefense(commands, zoneToReinforce);
        }
    }

    private void mvtDefense(List<CommandMvt> commands, TreeSet<Zone> zoneToReinforce) {
        Iterator<Zone> it = zoneToReinforce.iterator();
        while (it.hasNext()) {
            Zone destination = it.next();
            if (!Utils.isMine(destination))
                continue;
            reinforceZone(commands, destination, destination.additionnalDronesNeeded());
        }
    }

    /**
     * Pull from all adjacent until there is nothing to pull or until the need has been filled
     * @param commands
     * @param destination
     * @param needed
     */
    private void reinforceZone(List<CommandMvt> commands, Zone destination, int needed) {
        while (needed > 0) {
            TreeSet<PullResolver> candidates = new TreeSet<>();
            for (Zone z : destination.adjacentZones) {
                PullResolver pullResolver = z.getWillingless(destination);
                if (pullResolver != null && pullResolver.willingless > 0)
                    candidates.add(pullResolver);
            }
            if (candidates.size() > 0) {
                Zone origin = candidates.first().zone;
                System.err.println("Mvt defense : " + origin.id + " to : " + destination.id);
                sendDrone(commands, origin, destination);
                needed--;
            } else
                break;
        }
    }

    private void mvtDistant(List<CommandMvt> commands, List<Zone> zonesWithDrones) {
        Iterator<Zone> it = zonesWithDrones.iterator();
        while (it.hasNext()) {
            Zone origin = it.next();
            int i = origin.podsToKeep();
            for (; i < origin.getDrones(); i++) {
                MagnetismResolver magnetismResolver = origin.getDistantToGoTo();
                if (magnetismResolver != null) {
                    sendDrone(commands, origin, magnetismResolver.adjacent);
                    System.err.println("Distant : " + origin.id + " -> " + magnetismResolver.adjacent.id + "    TARGET : " + magnetismResolver.target.id);
                }
            }
        }
    }

    /**
     * idée : parcourir la liste en arrière
     * @param commands
     * @param zonesWithDrones
     */
    private void mvtAdjacent(List<CommandMvt> commands, List<Zone> zonesWithDrones) {
        Iterator<Zone> it = zonesWithDrones.iterator();

        while (it.hasNext()) {
            Zone origin = it.next();
            int pop = 0;
            for (int i = origin.podsToKeep(); i < origin.getDrones(); i++) {
                Zone zoneToGo = origin.getAdjacenToGoTo();
                if (zoneToGo != null) {
                    pop++;
                    sendDrone(commands, origin, zoneToGo);
                    zoneToGo.targetted++;
                    System.err.println("Adjacent : " + origin.id + " -> " + zoneToGo.id + " with future drones : " + zoneToGo.futurDrones);
                }
            }
            if (origin.getDrones() == pop)
                it.remove();
        }
    }

    private void mvtAdjacentOpti(List<CommandMvt> commands, List<Zone> zonesWithDrones) {
        Iterator<Zone> it = zonesWithDrones.iterator();

        List<Drone> drones = new ArrayList<>();
        while (it.hasNext())
            drones.addAll(it.next().getDronesWithAdjacentMvt());

        Collections.sort(drones, new Comparator<Drone>() {
            @Override
            public int compare(Drone o1, Drone o2) {
                int diff = o1.adjacentDestinations.size() - o2.adjacentDestinations.size();
                return diff;
            }
        });

        List<Zone> alreadyTaken = new ArrayList<>();

        for (Drone drone : drones) {
            for (AdjacentMvt destination : drone.getDestinations()) {
                if (alreadyTaken.contains(destination.destination))
                    continue;
                alreadyTaken.add(destination.destination);
                sendDrone(commands, drone.currentPosition, destination.destination);
                System.err.println("Adjacent : " + drone.currentPosition.id + " -> " + destination.destination.id);
                break;
            }
        }
    }

    private void sendDrone(List<CommandMvt> commands, Zone origin, Zone zoneToGo) {
        CommandMvt commandMvt = new CommandMvt();
        commandMvt.from = origin.id;
        commandMvt.to = zoneToGo.id;
        commandMvt.drones = 1;
        commands.add(commandMvt);

        zoneToGo.updateFuturDrones(1);
        origin.updateDrones(-1);
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

    void spawnDrones(int platinium) {
        List<CommandSpawn> commands = new ArrayList<>();
        // defense
        platinium = defense(commands, platinium);

        TreeSet<ContinentSpawnAnalytic> spawnAnalytics = new TreeSet<>();
        for (Continent c : disputed)
            spawnAnalytics.add(c.getSpawnAnalytics());

        if (spawnAnalytics.isEmpty())
            return;
        while (platinium >= Player.DRONE_COST)
            platinium = spawnDrone(platinium, commands, spawnAnalytics);

        Utils.executeCommands(commands);
    }

    private int defense(List<CommandSpawn> commands, int platinium) {
        TreeSet<Zone> zoneToReinforce = new TreeSet<>();
        for (Continent c : disputed) {
            for (Zone z : c.zoneWithRessources) {
                if (!Utils.isMine(z))
                    continue;
                zoneToReinforce.add(z);
            }
        }

        Iterator<Zone> it = zoneToReinforce.iterator();
        while (it.hasNext()) {
            Zone z = it.next();

        }
        return platinium;
    }

    private int spawnDrone(int platinum, List<CommandSpawn> commands, TreeSet<ContinentSpawnAnalytic> spawnAnalytics)    {

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
    List<Zone> zoneWithDrones = new ArrayList<>(), zoneWithRessources = new ArrayList<>();
    int group = 0;

    void addZone(Zone z) {
        zones.put(z.id, z);
        neutralZones.put(z.id, z);
        ressources += z.platinium;
        z.continent = this;
        if (z.platinium > 0)
            zoneWithRessources.add(z);
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
            spawnAnalytic.addCandidate(getSpawnResolver(z));
        for (Zone z : controlledZones.values())
            spawnAnalytic.addCandidate(getSpawnResolver(z));
        if (drones[Player.myId] == 0)
            spawnAnalytic.totalValue *= NO_DRONES_SPAWN_MULTI;
        return spawnAnalytic;
    }

    private SpawnResolver getSpawnResolver(Zone z) {
        z.spawnResolver.magnetism = z.evaluateFreeZone(otherPlayersActive);
        return z.spawnResolver;
    }

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

class Zone implements Comparable<Zone> {

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
    int platinium, ownerId = -1, platiniumNearby = 0, nbEnemies, futurDrones = 0, targetted = 0;
    int[] drones = new int[4], adjacentDrones = new int[4];
    List<Zone> adjacentZones = new ArrayList<>(), adjacentWithRessources = new ArrayList<>(), adjacentOfAdjacentWithRessources = new ArrayList<>();
    boolean justBeenTaken = false;
    public Continent continent;

    Zone(int id, int platinium) {
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
        reset();
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            nbEnemies += drones[i];
        }
        return Utils.determineStatus(this);
    }

    private void reset() {
        futurDrones = 0;
        nbEnemies = 0;
        targetted = 0;
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
            if (Utils.hasEnemiesNearby(this) && drones[Player.myId] < Utils.getNbEnemyDronesNearby(this) + 2)
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
            z.examineZone(getDrones(), candidates, z, id);
        if (candidates.size() > 0)
            return candidates.first();
        return null;
    }

    private void examineZone(int drones, TreeSet<MagnetismResolver> candidates, Zone adjacent, int... ids) {
        for (int i : ids)
            if (i == id)
                return;
//        if (Utils.getEnemieDrones(this.drones) >= drones)
//            return;
        float magnetism = getMagnetism();
        int[] newIds = new int[ids.length + 1];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        newIds[newIds.length - 1] = id;
        if (magnetism > 0)
            candidates.add(new MagnetismResolver(magnetism / newIds.length * 2, adjacent, this));

        if (newIds.length >= MAX_DISTANCE)
            return;

        for (Zone z : adjacentZones)
            z.examineZone(drones, candidates, adjacent, newIds);
    }

    private float getMagnetism() {
        if (Utils.getOtherPlayerActive(adjacentDrones) > 1 && drones[Player.myId] < 5)
            return -1;
        float i = 0;
        if (Utils.isMine(this)) {
            if (Utils.isBorder(this))
                i++;
            return i;
        }
        if (!Utils.isMine(this))
            i += 10;
        if (!Utils.hasEnemies(this))
            i += 5 + platinium * 5;
        else
            i += platinium;
        i /= (futurDrones / 2f) + 1;
        i /= (targetted / 4f) + 1;
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
//        if (Utils.allAdjacentAreMine(this) && Player.playerCount == 2)
//            return -1;
        int value = 0;

        for (Zone z : adjacentWithRessources) {
            if (!Utils.hasEnemies(z) && !Utils.isMine(z)) {
                value += (z.platinium * 4) / (1 + z.futurDrones);
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
            value *= platinium + 1;
        else
            value /= Utils.getNbEnemieZonesNearby(this) + 1;
        value /= futurDrones * 2 + 1;
        if (continent.drones[Player.myId] == 0)
            value *= 2;
        if (continent.zones.size() < 50 && Player.playerCount > 2)
            value *= 2;
//        if (Player.playerCount > 2 && Utils.isFree(this))
//            value *= 0.75f;
        return value;
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

    public int additionnalDronesNeeded() {
        if (platinium == 0)
            return 0;
        int needed = (int) (Utils.getNbEnemieZonesNearby(this) * 1.5f);
        return needed - drones[Player.myId];
    }

    /**
     * I can go if :
     *  I have no enemy zone nearby
     *  I have no platinium
     *  I have platinium but no enemies nearby
     *  I have platinium, enemies nearby, but enough drones to handle them
     *  I have platinium, enemies nearby, not enough drones to handle them but adjacent one has more platinium
     * @param destination
     * @return
     */
    public PullResolver getWillingless(Zone destination) {
        if (drones[Player.myId] == 0)
            return null;
        float willingless = destination.platinium - platinium;
        willingless += Utils.getNbEnemieZonesNearby(this) - (drones[Player.myId] + futurDrones);
        if (Utils.getNbEnemieZonesNearby(this) == 0)
            willingless += 10;
        if (willingless > 0) {
            PullResolver pullResolver = new PullResolver(this);
            pullResolver.willingless = willingless;
            return pullResolver;
        }
        return null;
    }

    @Override
    public int compareTo(Zone zone) {
        return zone.platinium - platinium;
    }

    public void updateFuturDrones(int i) {
        futurDrones += i;
    }

    public void updateDrones(int i) {
        drones[Player.myId] += i;
        if (drones[Player.myId] <= 0)
            continent.zoneWithDrones.remove(this);
    }

    public List<Drone> getDronesWithAdjacentMvt() {
        List<Drone> dronesList = new ArrayList<>();
        for (int i = 0; i < drones[Player.myId]; i++) {
            Drone drone = new Drone(this);
            drone.adjacentDestinations.addAll(getDronePossibleAdjacentDestinations());
            dronesList.add(drone);
        }
        return dronesList;
    }

    private List<AdjacentMvt> getDronePossibleAdjacentDestinations() {
        List<AdjacentMvt> possibilities = new ArrayList<>();

        if (platinium > 0 && Utils.hasEnemiesNearby(this) && drones[Player.myId] < Utils.getNbEnemyDronesNearby(this) + 2)
            possibilities.add(new AdjacentMvt(this, 10));

        if (Utils.getOtherPlayerActive(adjacentDrones) > 1)
            return possibilities;

        List<Zone> zones = adjacentZones;
        if (Player.playerCount < 4)
            zones = adjacentWithRessources;
        for (Zone z : zones) {
            if (z.futurDrones != 0 || Utils.isMine(z) || (Utils.hasEnemies(z)))
                continue;
            possibilities.add(new AdjacentMvt(z, (1 + z.platinium) / z.adjacentZones.size()));
        }
        Collections.sort(possibilities, new Comparator<AdjacentMvt>() {
            @Override
            public int compare(AdjacentMvt o1, AdjacentMvt o2) {
                return (int) ((o2.fitness * 1000) - (o1.fitness * 1000));
            }
        });
        return possibilities;
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
    public int compareTo(MagnetismResolver o) {
        return (int) ((o.magnetism * 1000) - (magnetism * 1000));
    }
}

class ContinentSpawnAnalytic implements Comparable<ContinentSpawnAnalytic> {
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
        return (o.totalValue * 1000) - (totalValue * 1000);
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

class PullResolver implements Comparable<PullResolver> {
    int dronesToSpare;
    float willingless;
    final Zone zone;

    PullResolver(Zone zone) {
        this.zone = zone;
    }

    @Override
    public int compareTo(PullResolver pullResolver) {
        return (int) ((pullResolver.willingless * 1000) - (willingless * 1000));
    }
}

class Drone {
    Zone currentPosition, futurePosition;
    List<AdjacentMvt> adjacentDestinations = new ArrayList<>();

    public Drone(Zone currentPosition) {
        this.currentPosition = currentPosition;
    }

    public List<AdjacentMvt> getDestinations() {
        Collections.sort(adjacentDestinations);
        return adjacentDestinations;
    }
}

class AdjacentMvt implements Comparable<AdjacentMvt> {
    Zone destination;
    float fitness;

    public AdjacentMvt(Zone destination, float fitness) {
        this.destination = destination;
        this.fitness = fitness;
    }

    @Override
    public int compareTo(AdjacentMvt o) {
        if (o.fitness == fitness)
            return destination.id - o.destination.id;
        return (int) ((o.fitness * 1000) - (fitness * 1000));
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
            if (i != Player.myId && drones[i] > 0)
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