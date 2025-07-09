package com.devstaq.auth.api.provider;

import com.devstaq.auth.api.data.ApiTestData;
import com.devstaq.auth.api.data.DataStatus;
import com.devstaq.auth.api.provider.holder.ApiTestArgumentsHolder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class ApiTestUpdatePasswordArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
                new ApiTestArgumentsHolder(
                        ApiTestData.getPasswordDto(),
                        DataStatus.VALID,
                        ApiTestData.passwordUpdateSuccess()
                ),
                new ApiTestArgumentsHolder(
                        ApiTestData.getInvalidPasswordDto(),
                        DataStatus.INVALID,
                        ApiTestData.passwordUpdateFailry()
                )
        ).map(Arguments::of);
    }
}
