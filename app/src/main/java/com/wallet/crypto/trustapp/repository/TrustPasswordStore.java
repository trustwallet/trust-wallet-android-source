package com.wallet.crypto.trustapp.repository;

import android.content.Context;

import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.util.KS;

import java.security.SecureRandom;

import io.reactivex.Completable;
import io.reactivex.Single;

public class TrustPasswordStore implements PasswordStore {

	private final Context context;

	public TrustPasswordStore(Context context) {
		this.context = context;
	}

    @Override
	public Single<String> getPassword(Wallet wallet) {
		return Single.fromCallable(() -> new String(KS.get(context, wallet.address)));
	}

	@Override
	public Completable setPassword(Wallet wallet, String password) {
		return Completable.fromAction(() -> KS.put(context, wallet.address, password));
	}

	@Override
	public Single<String> generatePassword() {
		return Single.fromCallable(() -> {
            byte bytes[] = new byte[256];
            SecureRandom random = new SecureRandom();
            random.nextBytes(bytes);
            return new String(bytes);
        });
	}
}
