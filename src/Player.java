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

    static final int DRONE_COST = 20;
    static int playerCount, myId;
    static final Random R = new Random();
    static boolean firstTurn = true;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        final World world = init(in);

        /**
         * Main loop
         */
        while (true)
            turn(in, world);
    }

    private static void turn(Scanner in, World world) {
        int platinum = in.nextInt();
        // keep at least one drone for next turn
        if (firstTurn)
            platinum -= DRONE_COST;
        in.nextLine();
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
        firstTurn = false;
    }

    private static World init(Scanner in) {
        playerCount = in.nextInt(); // the amount of players (2 to 4)
        myId = in.nextInt(); // my player ID (0, 1, 2 or 3)
        int zoneCount = in.nextInt(); // the amount of zones on the map
        int linkCount = in.nextInt(); // the amount of links between all
        in.nextLine();
        return new World(in, zoneCount, linkCount);
    }

}

/***
 *
 *    ██╗    ██╗ ██████╗ ██████╗ ██╗     ██████╗
 *    ██║    ██║██╔═══██╗██╔══██╗██║     ██╔══██╗
 *    ██║ █╗ ██║██║   ██║██████╔╝██║     ██║  ██║
 *    ██║███╗██║██║   ██║██╔══██╗██║     ██║  ██║
 *    ╚███╔███╔╝╚██████╔╝██║  ██║███████╗██████╔╝
 *     ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═════╝
 */
class World {

    private final int zoneCount;
    private final List<Continent> continents = new ArrayList<>(), disputed = new ArrayList<>();

    /***
     *     _____         _  _
     *    |_   _|       (_)| |
     *      | |   _ __   _ | |_
     *      | |  | '_ \ | || __|
     *     _| |_ | | | || || |_
     *     \___/ |_| |_||_| \__|
     */


    World(Scanner in, int zoneCount, int linkCount) {
        this.zoneCount = zoneCount;
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

    private Continent getAttachedContinent(Zone zone) {
        for (Continent c : continents)
            if (c.zones.containsValue(zone))
                return c;
        return null;
    }

    private Continent getContinentOfAdjacent(Zone zone) {
        for (Zone z : zone.getAllAdjacentZones()) {
            Continent c = getAttachedContinent(z);
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

    /***
     *     _   _             _         _
     *    | | | |           | |       | |
     *    | | | | _ __    __| |  __ _ | |_   ___
     *    | | | || '_ \  / _` | / _` || __| / _ \
     *    | |_| || |_) || (_| || (_| || |_ |  __/
     *     \___/ | .__/  \__,_| \__,_| \__| \___|
     *           | |
     *           |_|
     */
    void update(Scanner in) {
        for (Continent c : continents)
            c.reset();
        for (int i = 0; i < zoneCount; i++)
            updateZone(in);

        disputed.clear();

        for (Continent c : continents) {
            Utils.updateStatus(c);
            addContinentToList(c);
            c.updateFinished();
        }
    }

    private void updateZone(Scanner in) {
        int zId = in.nextInt(); // this zone's ID
        int ownerId = in.nextInt(); // the player who owns this zone (-1 otherwise)
        int[] drones = {in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
        in.nextLine();

        for (Continent c : continents)
            c.update(zId, ownerId, drones);
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
        List<Zone> zonesWithMyDrones = getZonesWithMyDrones();

        mvtToAdjacentZone(commands, zonesWithMyDrones);
        mvtToDistantZone(commands, zonesWithMyDrones);
        Utils.executeCommands(commands);
    }

    private List<Zone> getZonesWithMyDrones() {
        List<Zone> zonesWithDrones = new ArrayList<>();
        for (Continent c : disputed)
            zonesWithDrones.addAll(c.zoneWithMyDrones);
        return zonesWithDrones;
    }

    private void mvtToAdjacentZone(List<CommandMvt> commands, List<Zone> zonesWithMyDrones) {
        while (!zonesWithMyDrones.isEmpty()) {
            orderByPossibilities(zonesWithMyDrones);

            Zone zone = zonesWithMyDrones.get(0);
            if (zone.adjacentPossibilities.isEmpty()) {
                zonesWithMyDrones.remove(zone);
            } else {
                Utils.sendDrone(commands, zone, zone.adjacentPossibilities.get(0).destination);
                if (zone.drones[Player.myId] == 0)
                    zonesWithMyDrones.remove(zone);
            }
        }
    }

    /**
     * You don't want a zone with 2 possibilities taking the one possibility from an adjacent zone
     * @param zonesWithMyDrones
     */
    private void orderByPossibilities(List<Zone> zonesWithMyDrones) {
        for (Zone z : zonesWithMyDrones)
            z.buildUpAdjacentPossibilities();
        Collections.sort(zonesWithMyDrones, new Comparator<Zone>() {
            @Override
            public int compare(Zone o1, Zone o2) {
                return o1.adjacentPossibilities.size() - o2.adjacentPossibilities.size();
            }
        });
    }

    private void mvtToDistantZone(List<CommandMvt> commands, List<Zone> zonesWithMyDrones) {
        for (Continent c : disputed)
            zonesWithMyDrones.addAll(c.zoneWithMyDrones);
        Zone.setMaxDistance(zonesWithMyDrones);
        for (Zone origin : zonesWithMyDrones)
            origin.distantMvt(commands);
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
     */
    void spawnDrones(int platinium) {
        List<CommandSpawn> commands = new ArrayList<>();

        while (platinium >= Player.DRONE_COST) {
            List<SpawnResolver> spawns = getSpawningPoints();

            if (spawns.isEmpty())
                break;
            Collections.sort(spawns);
            platinium = spawnDrone(platinium, commands, spawns.get(0));
        }
        Utils.executeCommands(commands);
    }

    private List<SpawnResolver> getSpawningPoints() {
        List<SpawnResolver> spawns = new ArrayList<>();

        if (Player.playerCount > 2 && Player.firstTurn)
            firstTurnFocusSingleContinent(spawns);
        else
            for (Continent c : disputed)
                spawns.addAll(c.getSpawnAnalytics());
        return spawns;
    }

    private void firstTurnFocusSingleContinent(List<SpawnResolver> spawns) {
        Continent selectedFocus = null;
        int maxResource = 0;
        for (Continent c : disputed) {
            if (c.zones.size() >= zoneCount * 0.5f && c.resources > maxResource) {
                maxResource = c.resources;
                selectedFocus = c;
            }
        }
        if (selectedFocus != null)
            spawns.addAll(selectedFocus.getSpawnAnalytics());
    }

    private int spawnDrone(int platinum, List<CommandSpawn> commands, SpawnResolver spawnResolver)    {
        CommandSpawn commandSpawn = new CommandSpawn();
        commandSpawn.drones = 1;
        if (Utils.isMine(spawnResolver.zone))
            commandSpawn.drones++;
        commandSpawn.to = spawnResolver.zone.id;
        spawnResolver.zone.updateFutureDrones(commandSpawn.drones);
        commands.add(commandSpawn);
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
 */

class Continent {

    int resources = 0, futureDrones = 0, otherActivePlayers;
    int[] dronesTotal = new int[4];
    ContinentStatus status = ContinentStatus.DISPUTED;
    Map<Integer, Zone> zones = new HashMap<>(), neutralZones = new HashMap<>(), hostileZones = new HashMap<>(), controlledZones = new HashMap<>();
    List<Zone> zoneWithMyDrones = new ArrayList<>(), zoneWithResources = new ArrayList<>();
    int group = 0;

    /***
     *     _____         _  _
     *    |_   _|       (_)| |
     *      | |   _ __   _ | |_
     *      | |  | '_ \ | || __|
     *     _| |_ | | | || || |_
     *     \___/ |_| |_||_| \__|
     */

    void addZone(Zone z) {
        zones.put(z.id, z);
        neutralZones.put(z.id, z);
        resources += z.platinium;
        z.continent = this;
        if (z.platinium > 0)
            zoneWithResources.add(z);
    }

    public void initFinished() {
        for (Zone z : zones.values())
            z.initFinished();
    }

    /***
     *     _   _             _         _
     *    | | | |           | |       | |
     *    | | | | _ __    __| |  __ _ | |_   ___
     *    | | | || '_ \  / _` | / _` || __| / _ \
     *    | |_| || |_) || (_| || (_| || |_ |  __/
     *     \___/ | .__/  \__,_| \__,_| \__| \___|
     *           | |
     *           |_|
     */

    public void reset() {
        for (int i = 0; i < dronesTotal.length; i++)
            dronesTotal[i] = 0;
        futureDrones = 0;
        zoneWithMyDrones.clear();
        group = 0;
    }
    /**
     * @return true if the zone has been updated
     */
    boolean update(int zId, int ownerId, int[] drones) {
        if (zones.containsKey(zId)) {
            Zone zone = zones.get(zId);
            ZoneStatus previousStatus = zone.status;
            ZoneStatus newStatus = zone.update(ownerId, drones);
            if (previousStatus != newStatus)
                Utils.zoneStatusChanged(this, previousStatus, newStatus, zone);

            Utils.arrayAddition(dronesTotal, drones);
            if (zone.drones[Player.myId] > 0)
                zoneWithMyDrones.add(zone);
            return true;
        }
        return false;
    }

    void updateFinished() {
        otherActivePlayers = Utils.getOtherPlayerActive(dronesTotal);
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

    public List<SpawnResolver> getSpawnAnalytics() {
        List<SpawnResolver> spawnResolvers = new ArrayList<>();
        for (Zone z : neutralZones.values())            spawnResolvers.add(getSpawnResolver(z));
        for (Zone z : controlledZones.values())         spawnResolvers.add(getSpawnResolver(z));
        return spawnResolvers;
    }

    private SpawnResolver getSpawnResolver(Zone z) {
        z.spawnResolver.magnetism = z.evaluateFreeZone();
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
 */

class Zone {

    static int MAX_DISTANCE = 7;

    ZoneStatus status = ZoneStatus.NEUTRAL;
    SpawnResolver spawnResolver = new SpawnResolver(this);
    final int id;
    int platinium, ownerId = -1, platiniumNearby = 0, nbEnemies, futureDrones = 0, targeted = 0;
    int[] drones = new int[4], adjacentDrones = new int[4];
    List<Zone> adjacentZones = new ArrayList<>(), adjacentWithResources = new ArrayList<>(), adjacentOfAdjacentWithResources = new ArrayList<>();
    boolean justBeenTaken = false;
    public Continent continent;
    List<AdjacentMvt> adjacentPossibilities = new ArrayList<>();

    /***
     *     _____         _  _
     *    |_   _|       (_)| |
     *      | |   _ __   _ | |_
     *      | |  | '_ \ | || __|
     *     _| |_ | | | || || |_
     *     \___/ |_| |_||_| \__|
     */

    Zone(int id, int platinium) {
        this.platinium = platinium;
        this.id = id;
    }

    void addAdjacentZone(Zone zone) {
        adjacentZones.add(zone);
        if (zone.platinium > 0) {
            adjacentWithResources.add(zone);
            platiniumNearby += zone.platinium;
        }
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

    public void initFinished() {
        for (Zone z : adjacentZones)
            for (Zone distant : z.adjacentWithResources)
                if (!distant.equals(this))
                    adjacentOfAdjacentWithResources.add(distant);
    }

    int getDrones() {
        return drones[Player.myId];
    }

    /***
     *     _   _             _         _
     *    | | | |           | |       | |
     *    | | | | _ __    __| |  __ _ | |_   ___
     *    | | | || '_ \  / _` | / _` || __| / _ \
     *    | |_| || |_) || (_| || (_| || |_ |  __/
     *     \___/ | .__/  \__,_| \__,_| \__| \___|
     *           | |
     *           |_|
     */

    ZoneStatus update(int ownerId, int[] drones) {
        justBeenTaken = this.ownerId == Player.myId && ownerId != this.ownerId;
        this.ownerId = ownerId;
        System.arraycopy(drones, 0, this.drones, 0, drones.length);
        reset();
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            nbEnemies += drones[i];
        }
        return Utils.determineStatus(this);
    }

    private void reset() {
        futureDrones = 0;
        nbEnemies = 0;
        targeted = 0;
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
     */

    public static void setMaxDistance(List<Zone> zonesWithDrones) {
        if (zonesWithDrones.size() > 25)            Zone.MAX_DISTANCE = 6;
        else if (zonesWithDrones.size() > 10)       Zone.MAX_DISTANCE = 7;
        else                                        Zone.MAX_DISTANCE = 8;
    }

    public void buildUpAdjacentPossibilities() {
        adjacentPossibilities.clear();
        adjacentPossibilities.addAll(getDronePossibleAdjacentDestinations());
    }

    private List<AdjacentMvt> getDronePossibleAdjacentDestinations() {
        List<AdjacentMvt> possibilities = new ArrayList<>();
        if (shouldStayAndDefendZone())
            possibilities.add(new AdjacentMvt(this, getDefendValue()));

        if (letThemFightItOut())
            return possibilities;

        for (Zone z : adjacentZones) {
            if (z.shouldAlreadyBeOverwhelmed() || z.fabianStrategy() || z.notAnInterstingTarget())
                continue;
            evalAdjacentMvt(possibilities, z);
        }

        Collections.sort(possibilities, new Comparator<AdjacentMvt>() {
            @Override
            public int compare(AdjacentMvt o1, AdjacentMvt o2) {
                if (o2.fitness > o1.fitness)    return 1;
                if (o1.fitness > o2.fitness)    return -1;
                return 0;
            }
        });
        return possibilities;
    }

    private boolean letThemFightItOut() {
        return Utils.getOtherPlayerActive(adjacentDrones) > 1;
    }

    private int getDefendValue() {
        return 10 + platinium * 8;
    }

    private boolean shouldStayAndDefendZone() {
        return platinium > 0 && Utils.hasEnemiesNearby(this) && drones[Player.myId] < Utils.getNbEnemyDronesNearby(this) + 1 && futureDrones <= 4;
    }

    private boolean shouldMoveAndDefend() {
        return Utils.hasEnemiesNearby(this) && getDrones() == 0 && Utils.isMine(this) && futureDrones == 0;
    }

    private boolean shouldAlreadyBeOverwhelmed() {
        return futureDrones > Utils.getEnemyDrones(drones);
    }

    private boolean fabianStrategy() {
        return (Utils.hasEnemies(this) && Player.playerCount > 2 && drones[Player.myId] < 4);
    }

    private boolean notAnInterstingTarget() {
        return Utils.isMine(this) && (platinium == 0 || Utils.getNbEnemyDronesNearby(this) == 0);
    }

    private void evalAdjacentMvt(List<AdjacentMvt> possibilities, Zone z) {
        AdjacentMvt adjacentMvt = new AdjacentMvt(z, 1 + (z.platinium * 4f));

        if (Utils.isFree(z))
            adjacentMvt.fitness += 1 + (z.platinium * 6f);

        if (z.shouldMoveAndDefend())
            adjacentMvt.fitness = platinium * 3f;

        for (Zone z2 : z.adjacentZones) {
            if (!Utils.isMine(z2))  adjacentMvt.fitness += z2.platinium * 2;
            else                    adjacentMvt.fitness += z2.platinium;
        }

        adjacentMvt.fitness /= getFitnessWeight(z);
        possibilities.add(adjacentMvt);
    }

    private float getFitnessWeight(Zone z) {
        return 1f + ((Utils.getNbEnemiesZonesNearby(z) - z.futureDrones) * (Player.playerCount - 1f));
    }








    void distantMvt(List<CommandMvt> commands) {
        for (int i = 0; i < getDrones(); i++) {
            MagnetismResolver magnetismResolver = getDistantToGoTo();
            if (magnetismResolver != null)  Utils.sendDrone(commands, this, magnetismResolver.adjacent);
            else                            updateFutureDrones(1);
        }
    }

    public MagnetismResolver getDistantToGoTo() {
        if (platinium > 0)
            if (Utils.hasEnemiesNearby(this) && futureDrones < Utils.getNbEnemyDronesNearby(this))
                return null;

        List<MagnetismResolver> candidates = new ArrayList<>();

        for (Zone z : adjacentZones)
            z.determinePath(0, candidates, z, id);

        if (candidates.size() > 0) {
            Collections.sort(candidates, new Comparator<MagnetismResolver>() {
                @Override
                public int compare(MagnetismResolver o1, MagnetismResolver o2) {
                    if (o2.magnetism > o1.magnetism)                        return 1;
                    if (o1.magnetism > o2.magnetism)                        return -1;
                    return 0;
                }
            });
            return candidates.get(0);
        }
        return new MagnetismResolver(0.1f, adjacentZones.get( Player.R.nextInt(adjacentZones.size())), this);
    }

    private void determinePath(float magnetismThisFar, List<MagnetismResolver> candidates, Zone adjacent, int... ids) {
        for (int i : ids)
            if (i == id)
                return;
        float magnetism = getMagnetism();
        int[] newIds = new int[ids.length + 1];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        newIds[newIds.length - 1] = id;
        if (magnetism > 0)
            candidates.add(new MagnetismResolver(magnetismThisFar + (magnetism / newIds.length * 2), adjacent, this));
        if (newIds.length >= MAX_DISTANCE)
            return;

        for (Zone z : adjacentZones)
            z.determinePath(magnetismThisFar + (magnetism / newIds.length * 2), candidates, adjacent, newIds);
    }

    private float getMagnetism() {
        float i = 0;
        if (Utils.isMine(this)) {
            if (Utils.isBorder(this))
                i++;
            return i;
        }

        i += 6;
        if (Utils.isFree(this))             i++;
        if (!Utils.hasEnemies(this))        i += 5 + platinium * 5;
        else                                i += platinium * 2;

        i /= (futureDrones) + 1f;
        i /= (targeted / 4f) + 1;
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
     **/
    public float evaluateFreeZone() {
        // there is really no point in spawning on such a zone
        if (Utils.allAdjacentAreMine(this))
            return -1;
        float value = 1 + (platinium * 6);
        for (Zone z : adjacentWithResources)
            value = z.evaluateZoneAsAdjacentForSpawn(value);

        int dronesNearbyPrediction = drones[Player.myId] + (futureDrones * 2) + adjacentDrones[Player.myId];
        for (Zone z : adjacentZones)
            dronesNearbyPrediction += z.futureDrones;

        // if it's mine, it's valuable if it is in danger
        if (Utils.isMine(this))                         value = updateSpawnValueForMyZone(value);
        if (Utils.isFree(this))                         value = updateSpawnValueForFreeZone(value);

        // Player count : the more player, the more small continents will be important
        float percentage = (float) (-continent.futureDrones + (Player.playerCount - 2) + continent.controlledZones.size()) / (float) (continent.zones.size() + continent.futureDrones);

        value *= 1 + percentage;
        value /= 1 + dronesNearbyPrediction;
        if (!Player.firstTurn || Player.playerCount == 2)               value /= 1 + (adjacentZones.size() / 2);
        if (Player.playerCount == 2 && continent.zones.size() < 10)     value /= 2;
        // really improved
        if (Utils.hasLessDronesThanEnemies(continent))
            value *= 2;
        return value;
    }

    private float updateSpawnValueForMyZone(float value) {
        if (platinium == 0)                             value /= 10f;
        if (Utils.getNbEnemyDronesNearby(this) == 0)    value /= 10f;
        else                                            value = updateSpawnValueForZoneInDanger(value);
        return value;
    }

    private float updateSpawnValueForZoneInDanger(float value) {
        value += platinium * 40;
        for (Zone z : adjacentWithResources)
            value += z.platinium;
        return value;
    }

    private float updateSpawnValueForFreeZone(float value) {
        value++;
        value *= 2;
        value += platinium * 2;
        return value;
    }

    private float evaluateZoneAsAdjacentForSpawn(float value) {
        if (!Utils.isMine(this)) {
            value += platinium;
            if (!Utils.hasEnemies(this))
                value += platinium;
        }
        return value;
    }

    public void updateFutureDrones(int i) {
        futureDrones += i;
        continent.futureDrones += i;
    }

    public void updateDrones(int i) {
        drones[Player.myId] += i;
        if (drones[Player.myId] <= 0)
            continent.zoneWithMyDrones.remove(this);
    }

}
/***
 *     ██████╗ ██████╗ ███╗   ███╗███╗   ███╗ █████╗ ███╗   ██╗██████╗ ███████╗
 *    ██╔════╝██╔═══██╗████╗ ████║████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝
 *    ██║     ██║   ██║██╔████╔██║██╔████╔██║███████║██╔██╗ ██║██║  ██║███████╗
 *    ██║     ██║   ██║██║╚██╔╝██║██║╚██╔╝██║██╔══██║██║╚██╗██║██║  ██║╚════██║
 *    ╚██████╗╚██████╔╝██║ ╚═╝ ██║██║ ╚═╝ ██║██║  ██║██║ ╚████║██████╔╝███████║
 *     ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═════╝ ╚══════╝
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

    @Override
    public int compareTo(MagnetismResolver resolver) {
        if (resolver.magnetism > magnetism)
            return 1;
        if (magnetism > resolver.magnetism)
            return -1;
        return 0;
    }
}

class SpawnResolver implements Comparable<SpawnResolver> {
    float magnetism;
    final Zone zone;

                    public SpawnResolver(Zone zone) {                   this.zone = zone;                                       }
    @Override
    public int compareTo(SpawnResolver resolver) {
        if (resolver.magnetism > magnetism)
            return 1;
        if (magnetism > resolver.magnetism)
            return -1;
        return 0;
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
    public int compareTo(AdjacentMvt adjacentMvt) {
        if (adjacentMvt.fitness == fitness)
            return destination.id - adjacentMvt.destination.id;
        return (int) ((adjacentMvt.fitness * 100000) - (fitness * 100000));
    }
}

enum ContinentStatus {      PACIFIED, HOSTILE, DISPUTED     }
enum ZoneStatus {           NEUTRAL, HOSTILE, CONTROLLED    }

/***
 *    ██╗   ██╗████████╗██╗██╗     ███████╗
 *    ██║   ██║╚══██╔══╝██║██║     ██╔════╝
 *    ██║   ██║   ██║   ██║██║     ███████╗
 *    ██║   ██║   ██║   ██║██║     ╚════██║
 *    ╚██████╔╝   ██║   ██║███████╗███████║
 *     ╚═════╝    ╚═╝   ╚═╝╚══════╝╚══════╝
 */
class Utils {

    public static int[] arrayAddition(int[]... arrays) {
        int longest = -1;
        for (int[] array : arrays) {
            if (array.length > longest)
                longest = array.length;
        }

        int[] result = new int[longest];

        Arrays.fill(result, 0);

        for (int[] array : arrays) {
            for(int i = 0; i < array.length; i++)
                result[i] += array[i];
        }
        return result;
    }

    public static boolean hasEnemies(Zone zone) {
        for (int i = 0; i < zone.drones.length; i++)
            if (zone.drones[i] > 0 && i != Player.myId)
                return true;
        return false;
    }

    public static int getNbEnemiesZonesNearby(Zone zone) {
        int i = 0;
        for (Zone z : zone.adjacentZones)
            if (!Utils.isMine(z) && !Utils.isFree(z))
                i++;
        return i;
    }

    /**
     * Will update the {@link Zone#status}
     * @param zone
     * @return {@link Zone#status}
     */
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

    public static boolean isMine(Zone zone) {        return zone.status == ZoneStatus.CONTROLLED;                   }
    public static boolean isFree(Zone zone) {        return zone.status == ZoneStatus.NEUTRAL;                      }

    /**
     * Will only look on ADJACENT zones
     * @param zone
     * @return
     */
    public static boolean hasEnemiesNearby(Zone zone) {
        for (Zone z : zone.adjacentZones)
            if (Utils.hasEnemies(z))
                return true;
        return false;
    }

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

    /**
     * Sets the status to either : {@link ContinentStatus#PACIFIED}, {@link ContinentStatus#HOSTILE} or {@link ContinentStatus#DISPUTED}
     * @param continent
     * @return {@link Continent#status}
     */
    public static ContinentStatus updateStatus(Continent continent) {
        if (continent.controlledZones.size() == continent.zones.size())
            continent.status = ContinentStatus.PACIFIED;
        else if (continent.hostileZones.size() == continent.zones.size())
            continent.status = ContinentStatus.HOSTILE;
        else continent.status = ContinentStatus.DISPUTED;
        return continent.status;
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

    private static boolean txt = true;
    public static void executeCommands(List commands) {
        if (commands.size() > 0) {
            StringBuilder commandsOutput = new StringBuilder();
            for (Object c : commands)
                commandsOutput.append(c.toString());
            if (txt && Player.R.nextInt(10) == 5)
                // So fun <3 AYBABTU
                commandsOutput.append("#All your base are belong to us");
            txt = !txt;
            System.out.println(commandsOutput.toString());
        } else
            System.out.println("WAIT");
    }

    public static int getOtherPlayerActive(int[] drones) {
        int players = 0;
        for (int i = 0; i < drones.length; i++)
            if (i != Player.myId && drones[i] > 0)
                players++;
        return players;
    }

    public static int getEnemyDrones(int[] drones) {
        int cpt = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i == Player.myId)
                continue;
            if (drones[i] > cpt)
                cpt += drones[i];
        }
        return cpt;
    }

    /**
     * Only ADJACENT zones
     * @param zone
     * @return
     */
    public static int getNbEnemyDronesNearby(Zone zone) {
        int cpt = 0;
        for (Zone z : zone.adjacentZones) {
            if (z.ownerId == -1 || z.ownerId == Player.myId)
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

    public static void sendDrone(List<CommandMvt> commands, Zone origin, Zone zoneToGo) {
        CommandMvt commandMvt = new CommandMvt();
        commandMvt.from = origin.id;
        commandMvt.to = zoneToGo.id;
        commandMvt.drones = 1;
        commands.add(commandMvt);

        zoneToGo.updateFutureDrones(1);
        origin.updateDrones(-1);
    }
}