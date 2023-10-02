package six.cordcraft;

import net.fabricmc.api.ModInitializer;


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.text.LiteralTextContent;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.IOException;
import java.nio.file.Path;

import java.net.HttpURLConnection;

public class CordCraft implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("cordcraft");
	private static  String WEBHOOK_URL = null;
	private static final String CONFIG_FILE_NAME = "coordcraft.txt";
	private static File configFile;


	@Override
	public void onInitialize() {
			createConfigFile();

			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("ysl")
					.then(argument("name", StringArgumentType.word())
							.executes(context -> {
								WEBHOOK_URL = readDataFromFile();
								if(WEBHOOK_URL == null | WEBHOOK_URL == ""){
									context.getSource().sendMessage(Text.literal("Set the webhook use /webhook"));
									return 1;
								}

								ServerCommandSource source = context.getSource();
								PlayerEntity player = source.getPlayer();
								World world = player.getEntityWorld();
								RegistryKey<World> dimensionKey = world.getRegistryKey();
								String dimensionName = dimensionKey.getValue().toString();
								String biden = StringArgumentType.getString(context, "name");
								String message = getPlayerCoordinates(player,biden, dimensionName);
								sendToDiscordWebhook(message);
								context.getSource().sendMessage(Text.literal("Sent your coordinates"));
								return 1;
							}))));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("webhook")
					.then(CommandManager.argument("webhook", StringArgumentType.greedyString())
							.executes(context -> {
								String webhook = StringArgumentType.getString(context, "webhook");
								writeDataToFile(webhook);
								context.getSource().sendMessage(Text.literal("Saved webhook"));
								return 1;
							}));

			dispatcher.register(command);
		});
		}
	private void sendToDiscordWebhook(String message) {
		try {
			URL url = new URL(WEBHOOK_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			String jsonPayload = "{\"content\":\"" + message + "\"}";
			try (OutputStream outputStream = connection.getOutputStream()) {
				outputStream.write(jsonPayload.getBytes());
				outputStream.flush();
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
				System.out.println("Coordinates sent to Discord successfully.");
			} else {
				System.out.println("Failed to send coordinates to Discord. HTTP Response Code: " + responseCode);
			}

			connection.disconnect();
		} catch (IOException e) {
			System.err.println("Error sending coordinates to Discord webhook: " + e.getMessage());
		}
	}
	public static String getPlayerCoordinates(PlayerEntity player, String biden, String dim) {
		if (player != null) {

			Vec3d playerPos = player.getPos();
			double x = playerPos.x;
			double y = playerPos.y;
			double z = playerPos.z;
			int intX = (int) x;
			int intY = (int) y;
			int intZ = (int) z;


			String message = String.format("%d, %d, %d, %s, %s", intX, intY, intZ, dim, biden);
			return message;
		}
		return "";
	}
	public static void createConfigFile() {
		configFile = new File(FabricLoader.getInstance().getConfigDir().toString(), CONFIG_FILE_NAME);

		try {
			if (configFile.createNewFile()) {

			} else {

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String readDataFromFile() {
		try {
			FileReader reader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line = bufferedReader.readLine();
			bufferedReader.close();
			return line;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void writeDataToFile(String data) {
		try {
			FileWriter writer = new FileWriter(configFile, false);
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	}