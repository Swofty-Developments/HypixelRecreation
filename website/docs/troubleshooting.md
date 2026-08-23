# Troubleshooting

Common issues and their solutions.

## Connection Issues

### Redis Connection Failed

**Error**:
`redis.clients.jedis.exceptions.JedisConnectionException: Failed to connect to any host resolved for DNS name.`

**Solutions**:

1. Verify Redis/Memurai is running
2. Check the `redis-uri` in `config.yml`
3. On Windows, try [this Redis port](https://github.com/tporadowski/redis/releases) instead of Memurai

### Can't Connect to Server

**Symptoms**: Client times out or refuses connection

**Checklist**:

1. Is Velocity proxy running?
2. Is at least one game server running?
3. Is PicoLimbo running?
4. Check if the `velocity-secret` matches everywhere:
    - `forwarding.secret` (Velocity)
    - `config.yml` (game servers)
    - `server.toml` (PicoLimbo)

### MongoDB Connection Failed

**Solutions**:

1. Verify MongoDB is running on port 27017
2. Check `mongodb` in `config.yml`
3. If using authentication, include credentials in URI:
   ```
   mongodb://username:password@localhost:27017
   ```

## Gameplay Issues

### "You have strayed too far from spawn!"

**Cause**: The static region catalog is missing or invalid

**Solution**:

1. Ensure `configuration/skyblock/Minestom.regions.yml` exists
2. Restart the game server

### Data saving issues between changing servers

**Cause**: Running a Velocity build other than the one the project targets

**Solution**:

1. Download the Velocity 3.5.0-SNAPSHOT-605 build linked in [Proxy Setup](/docs/setup/proxy) — the same stock PaperMC build the Docker image uses
2. Replace your Velocity proxy with that version
3. Restart the proxy (which will also restart all game servers)

### Players Can't See Each Other

**Cause**: Multiple servers not syncing through Redis

**Solution**:

1. Verify Redis is running
2. Check all servers use the same `redis-uri`
3. Restart all game servers

### Auctions Not Working

**Cause**: Auction service not running

**Solution**:

1. Start `ServiceAuctionHouse.jar`
2. Verify MongoDB connection
3. Check for errors in service logs

### Party System Not Working

**Cause**: Party service not running

**Solution**:

1. Start `ServiceParty.jar`
2. Verify Redis connection
3. Check service logs for errors

## World Issues

### World Not Loading

**Checklist**:

1. World folder exists in correct location
2. World folder name matches expected name exactly
3. World contains valid region files

### Missing Hub Features

**Cause**: Required world data or static catalogs are missing

**Solution**:

- Ensure `configuration/skyblock/Minestom.fairysouls.yml` exists
- Ensure `configuration/skyblock/Minestom.regions.yml` exists
- Ensure `configuration/skyblock/Minestom.crystals.yml` exists

## Docker Issues

### Containers Won't Start

**Solutions**:

1. Ensure Docker Desktop is running
2. Check for port conflicts (25565, 27017, 6379, 8080)
3. Review logs: `docker-compose logs`
4. Try rebuilding: `docker-compose down && docker-compose up --build`

### Can't Connect to Docker Server

**Checklist**:

1. Proxy container is healthy
2. At least one game server container is running
3. Connect to `localhost:25565`

### Containers Keep Restarting

**Check logs**:

```bash
docker-compose logs -f <container_name>
```

Common causes:

- Missing configuration files
- Invalid `config.yml`
- Database connection failures

## Performance Issues

### High Memory Usage

**Solutions**:

1. Allocate appropriate memory per server:
   ```bash
   java -Xms2G -Xmx2G -jar HypixelCore.jar SKYBLOCK_HUB
   ```
2. Run fewer server instances
3. Reduce view distance in configuration

### Lag Spikes

**Common causes**:

1. Insufficient RAM
2. MongoDB on slow storage (use SSD)
3. Too many players per server instance

## Getting Help

If you're still having issues:

1. **Check logs** for specific error messages
2. **Search existing issues** on [GitHub](https://github.com/Swofty-Developments/HypixelRecreation/issues)
3. **Join Discord** at [discord.swofty.net](https://discord.swofty.net)
4. **Ask in #code-help** with:
    - Screenshots of all console outputs
    - Your `config.yml` (remove secrets)
    - Steps you've already tried

:::alert warning
Pinging staff members won't solve your issue faster!
:::
