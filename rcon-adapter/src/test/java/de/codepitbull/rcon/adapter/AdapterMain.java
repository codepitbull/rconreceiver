package de.codepitbull.rcon.adapter;

import com.hivemq.adapter.sdk.api.data.DataPoint;
import com.hivemq.adapter.sdk.api.model.ProtocolAdapterInput;
import com.hivemq.adapter.sdk.api.model.ProtocolAdapterStartOutput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingOutput;
import de.codepitbull.rcon.adapter.config.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdapterMain {
    public static void main(String[] args) throws Exception{

        var config = new RconProtocolAdapterSpecificConfiguration("tcp://127.0.0.1:27015", new Auth("iesaih3uY3esei3"), new RconToMqttConfig(100,3, false));

        ProtocolAdapterInput<RconProtocolAdapterSpecificConfiguration> input = mock(ProtocolAdapterInput.class);
        when(input.getConfig()).thenReturn(config);
        when(input.getTags()).thenReturn(List.of(new RconTag("taggy", "taggyyy", new RconTagDefinition(23.5, -37.5, 755, 1, "signal-A"))));
        var adapter = new RconProtocolAdapter(RconProtocolAdapterInformation.INSTANCE, input);

        var output = mock(ProtocolAdapterStartOutput.class);

        adapter.start(null, output);

        Thread.sleep(2000);

        var pollingOutput = new BatchPollingOutput() {
            @Override
            public void addDataPoint(@NotNull String s, @NotNull Object o) {
                System.out.println(s + " " +o);
            }

            @Override
            public void addDataPoint(@NotNull DataPoint dataPoint) {
                System.out.println(dataPoint);
            }

            @Override
            public void finish() {

            }

            @Override
            public void fail(@NotNull Throwable throwable, @Nullable String s) {

            }

            @Override
            public void fail(@NotNull String s) {

            }
        };

        adapter.poll(null, pollingOutput);

        Thread.sleep(3000);
    }
}
