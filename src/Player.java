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

    // Je veux du java 8 bordel !
    static long longestUpdate = 0, longestSpawn = 0, longestMvt = 0;
    static final int DRONE_COST = 20;
    static int playerCount, myId;
    static final Random R = new Random();
    static boolean firstTurn = true;
    static long beginTurn = 0;
    static Enemy[] enemies = new Enemy[4];

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        final World world = init(in);

        while (true) {
            int platinum = in.nextInt();
            if (firstTurn)
                platinum -= DRONE_COST;
            in.nextLine();
            long begin = System.currentTimeMillis();
            beginTurn = System.currentTimeMillis();
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
            firstTurn = false;
        }
    }

    private static World init(Scanner in) {
        playerCount = in.nextInt(); // the amount of players (2 to 4)
        for (int i = 0; i < enemies.length; i++)
            enemies[i] = new Enemy();
        myId = in.nextInt(); // my player ID (0, 1, 2 or 3)
        int zoneCount = in.nextInt(); // the amount of zones on the map
        int linkCount = in.nextInt(); // the amount of links between all
        in.nextLine();
        return new World(in, zoneCount, linkCount);
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
        for (int i = 0; i < Player.enemies.length; i++)
            Player.enemies[i].power = 0;
        for (Continent c : continents)
            c.update();
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
        List<Zone> zonesWithDrones = new ArrayList<>();
        for (Continent c : disputed)
            zonesWithDrones.addAll(c.zoneWithDrones);
        // ADJACENT
        adjacentMvt(commands, zonesWithDrones);
        // DISTANT
        for (Continent c : disputed)
            zonesWithDrones.addAll(c.zoneWithDrones);
        distantMvt(commands, zonesWithDrones);
        Utils.executeCommands(commands);
    }

    private void distantMvt(List<CommandMvt> commands, List<Zone> zonesWithDrones) {
        System.err.println("    drones : " + zonesWithDrones.size());
        if (zonesWithDrones.size() > 25)
            Zone.MAX_DISTANCE = 6;
        else if (zonesWithDrones.size() > 10)
            Zone.MAX_DISTANCE = 7;
        else
            Zone.MAX_DISTANCE = 8;
        for (Zone origin : zonesWithDrones) {
            int drones = origin.getDrones();
            for (int i = 0; i < drones; i++) {
                if (System.currentTimeMillis() - Player.beginTurn > 80)
                    Zone.MAX_DISTANCE--;
                MagnetismResolver magnetismResolver = origin.getDistantToGoTo();
                if (magnetismResolver != null) {
                    sendDrone(commands, origin, magnetismResolver.adjacent);
                    System.err.println("Distant : " + origin.id + " -> " + magnetismResolver.adjacent.id + "    TARGET : " + magnetismResolver.target.id);
                } else
                    origin.updateFuturDrones(1);
            }
        }
    }

    private void adjacentMvt(List<CommandMvt> commands, List<Zone> zonesWithDrones) {
        while (!zonesWithDrones.isEmpty()) {
            for (Zone z : zonesWithDrones)
                z.buildUpAdjacentPossibilities();
            Collections.sort(zonesWithDrones, new Comparator<Zone>() {
                @Override
                public int compare(Zone o1, Zone o2) {
                    return o1.adjacentPossibilities.size() - o2.adjacentPossibilities.size();
                }
            });

            if (!zonesWithDrones.isEmpty()) {
                Zone zone = zonesWithDrones.get(0);
                if (!zone.adjacentPossibilities.isEmpty()) {
                    AdjacentMvt adjacentMvt = zone.adjacentPossibilities.get(0);
                    sendDrone(commands, zone, adjacentMvt.destination);
                    System.err.println("Adjacent : " + zone.id + " -> " + adjacentMvt.destination.id);
                    if (zone.drones[Player.myId] == 0)
                        zonesWithDrones.remove(zone);
                } else
                    zonesWithDrones.remove(zone);
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
     */
    void spawnDrones(int platinium) {
        List<CommandSpawn> commands = new ArrayList<>();

        while (platinium >= Player.DRONE_COST) {
            List<SpawnResolver> spawns = new ArrayList<>();
            //*
            if (Player.playerCount > 2 && Player.firstTurn) {
                Continent selected = null;
                int maxResource = 0;
                for (Continent c : disputed) {
                    if (c.zones.size() == 33 || c.zones.size() == 44) {
                        if (c.ressources > maxResource) {
                            maxResource = c.ressources;
                            selected = c;
                        }
                    }
                }
                if (selected != null)
                    spawns.addAll(selected.getSpawnAnalytics());
            } else
            //*/
                for (Continent c : disputed)
                    spawns.addAll(c.getSpawnAnalytics());

            Collections.sort(spawns);
            if (spawns.isEmpty())
                break;
            platinium = spawnDrone(platinium, commands, spawns.get(0));
        }
        Utils.executeCommands(commands);
    }

    private int spawnDrone(int platinum, List<CommandSpawn> commands, SpawnResolver spawnResolver)    {
        CommandSpawn commandSpawn = new CommandSpawn();
        commandSpawn.drones = 1;
        if (Utils.isMine(spawnResolver.zone))
            commandSpawn.drones++;
        commandSpawn.to = spawnResolver.zone.id;
        spawnResolver.zone.updateFuturDrones(commandSpawn.drones);
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

    public void update() {
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
            if (ownerId != -1)
                Player.enemies[ownerId].power += zone.platinium;
            if (zone.drones[Player.myId] > 0)
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

    public List<SpawnResolver> getSpawnAnalytics() {
        List<SpawnResolver> spawnResolvers = new ArrayList<>();
        for (Zone z : neutralZones.values())
            spawnResolvers.add(getSpawnResolver(z));
        for (Zone z : controlledZones.values())
            spawnResolvers.add(getSpawnResolver(z));
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
    int platinium, ownerId = -1, platiniumNearby = 0, nbEnemies, futurDrones = 0, targetted = 0;
    int[] drones = new int[4], adjacentDrones = new int[4];
    List<Zone> adjacentZones = new ArrayList<>(), adjacentWithRessources = new ArrayList<>(), adjacentOfAdjacentWithRessources = new ArrayList<>();
    boolean justBeenTaken = false;
    public Continent continent;
    List<AdjacentMvt> adjacentPossibilities = new ArrayList<>();

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
        for (Zone z : adjacentZones)
            for (Zone distant : z.adjacentWithRessources)
                if (!distant.equals(this))
                    adjacentOfAdjacentWithRessources.add(distant);
    }

    ZoneStatus update(int ownerId, int podsP0, int podsP1, int podsP2, int podsP3) {
        justBeenTaken = this.ownerId == Player.myId && ownerId != this.ownerId;
        this.ownerId = ownerId;
//        for (int i = 0; i < 4; i++)
//            previousDrones[i] = drones[i];
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
     */
    public void buildUpAdjacentPossibilities() {
        adjacentPossibilities.clear();
        adjacentPossibilities.addAll(getDronePossibleAdjacentDestinations());
    }


    private List<AdjacentMvt> getDronePossibleAdjacentDestinations() {
        List<AdjacentMvt> possibilities = new ArrayList<>();
        if (platinium > 0 && Utils.hasEnemiesNearby(this) && drones[Player.myId] < Utils.getNbEnemyDronesNearby(this) + 1 && futurDrones <= 4)
            possibilities.add(new AdjacentMvt(this, 10 + platinium * 8));

        if (Utils.getOtherPlayerActive(adjacentDrones) > 1)
            return possibilities;

        for (Zone z : adjacentZones) {
            if (z.futurDrones > Utils.getEnemyDrones(z.drones)
                    || ((Utils.hasEnemies(z) && Player.playerCount > 2 && drones[Player.myId] < 4)))
                continue;

            AdjacentMvt adjacentMvt = new AdjacentMvt(z, 0);
            if (!Utils.isMine(z))
                adjacentMvt.fitness += 1 + (z.platinium * 4f);
            else {
                if (z.platinium == 0)                       continue;
                if (Utils.getNbEnemyDronesNearby(z) == 0)   continue;
            }

            if (Utils.isFree(z))
                adjacentMvt.fitness += 1 + (z.platinium * 6f);

            if (Utils.hasEnemiesNearby(z) && z.getDrones() == 0 && Utils.isMine(z) && z.futurDrones == 0)
                adjacentMvt.fitness = platinium * 3f;

            for (Zone z2 : z.adjacentZones) {
                if (!Utils.isMine(z2))  adjacentMvt.fitness += z2.platinium * 2;
                else                    adjacentMvt.fitness += z2.platinium;
            }

            adjacentMvt.fitness /= 1f + ((Utils.getNbEnemieZonesNearby(z) - z.futurDrones) * (Player.playerCount - 1f));
//            adjacentMvt.fitness /= z.adjacentZones.size();
            possibilities.add(adjacentMvt);
        }
        Collections.sort(possibilities, new Comparator<AdjacentMvt>() {
            @Override
            public int compare(AdjacentMvt o1, AdjacentMvt o2) {
                if (o2.fitness > o1.fitness)
                    return 1;
                if (o1.fitness > o2.fitness)
                    return -1;
                return 0;
            }
        });
        return possibilities;
    }

    public MagnetismResolver getDistantToGoTo() {
        if (platinium > 0)
            if (Utils.hasEnemiesNearby(this) && futurDrones < Utils.getNbEnemyDronesNearby(this))
                return null;
        List<MagnetismResolver> candidates = new ArrayList<>();
        for (Zone z : adjacentZones)
            z.examineZone(0, candidates, z, id);
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

    private void examineZone(float magnetismThisFar, List<MagnetismResolver> candidates, Zone adjacent, int... ids) {
        for (int i : ids)
            if (i == id)
                return;
//        if (Utils.getEnemyDrones(this.drones) >= drones)
//            return;
        float magnetism = getMagnetism();
        int[] newIds = new int[ids.length + 1];
        System.arraycopy(ids, 0, newIds, 0, ids.length);
        newIds[newIds.length - 1] = id;
        if (magnetism > 0)
            candidates.add(new MagnetismResolver(magnetismThisFar + (magnetism / newIds.length * 2), adjacent, this));
        if (newIds.length >= MAX_DISTANCE)
            return;

        for (Zone z : adjacentZones)
            z.examineZone(magnetismThisFar + (magnetism / newIds.length * 2), candidates, adjacent, newIds);
    }

    private float getMagnetism() {
        float i = 0;
        if (Utils.isMine(this)) {
            if (Utils.isBorder(this))
                i++;
            return i;
        }

        if (Utils.isFree(this))             i++;
        if (!Utils.isMine(this))            i += 5;
        if (!Utils.hasEnemies(this))        i += 5 + platinium * 5;
        else                                i += platinium * 2;
        if (!Utils.isMine(this))            i++;

        i /= (futurDrones) + 1f;
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
     **/
    public float evaluateFreeZone() {
        if (Utils.allAdjacentAreMine(this))
            return -1;
        float value = 1 + (platinium * 6);
        for (Zone z : adjacentWithRessources) {
            if (!Utils.isMine(z)) {
                value += z.platinium;
                if (!Utils.hasEnemies(z))
                    value += z.platinium;
            }
        }
        int dronesNearby = drones[Player.myId] + (futurDrones * 2) + adjacentDrones[Player.myId];
        for (Zone z : adjacentZones)
            dronesNearby += z.futurDrones;
        // Player count : the more player, the more small continents will be important
        float percentage = (float) (-continent.futurDrones + (Player.playerCount - 2) + continent.controlledZones.size()) / (float)( continent.zones.size() + continent.futurDrones);

        if (Utils.isMine(this)) {
            if (platinium == 0)                             value /= 10f;
            if (Utils.getNbEnemyDronesNearby(this) == 0)
                value /= 10f;
            else {
                // menacee
                value += platinium * 40;
                for (Zone z : adjacentWithRessources)
                    value += z.platinium;
            }
        }
        if (Utils.isFree(this)) {
            value++;
            value *= 2;
            value += platinium * 2;
        }
//        if (Player.playerCount > 2)
//            value /= Utils.getNbEnemieZonesNearby(this);
        value *= 1 + percentage;
        value /= 1 + dronesNearby;
        if (!Player.firstTurn || Player.playerCount == 2)
            value /= 1 + (adjacentZones.size() / 2);
        if (Player.playerCount == 2 && continent.zones.size() < 10)
            value /= 2;
        // really improved
        if (Utils.hasLessDronesThanEnemies(continent))
            value *= 2;
//        if (Utils.isBorder(this))
//            value *= 1.5f;
        return value;
    }

    public void updateFuturDrones(int i) {
        futurDrones += i;
        continent.futurDrones += i;
    }

    public void updateDrones(int i) {
        drones[Player.myId] += i;
        if (drones[Player.myId] <= 0)
            continent.zoneWithDrones.remove(this);
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
    public int compareTo(MagnetismResolver o) {
        if (o.magnetism > magnetism)
            return 1;
        if (magnetism > o.magnetism)
            return -1;
        return 0;
    }
}

class SpawnResolver implements Comparable<SpawnResolver> {
    float magnetism;
    final Zone zone;

                    public SpawnResolver(Zone zone) {                   this.zone = zone;                                       }
    @Override
    public int compareTo(SpawnResolver o) {
        if (o.magnetism > magnetism)
            return 1;
        if (magnetism > o.magnetism)
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
    public int compareTo(AdjacentMvt o) {
        if (o.fitness == fitness)
            return destination.id - o.destination.id;
        return (int) ((o.fitness * 100000) - (fitness * 100000));
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
 */
class Utils {

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

    public static boolean isMine(Zone zone) {        return zone.status == ZoneStatus.CONTROLLED;                   }
    public static boolean isFree(Zone zone) {        return zone.status == ZoneStatus.NEUTRAL;                      }

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

    public static ContinentStatus determineStatus(Continent continent) {
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
    public static void executeCommands(List<? extends Object> commands) {
        if (commands.size() > 0) {
            StringBuilder commandsOutput = new StringBuilder();
            for (Object c : commands)
                commandsOutput.append(c.toString());
            if (txt && Player.R.nextInt(10) == 5)
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
}

class Enemy {
    int power;
}