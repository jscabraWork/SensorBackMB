package com.arquitectura;

public class JwtConfig {

	
	public static final String RSA_PRIVATE="-----BEGIN RSA PRIVATE KEY-----\n" +
			"MIIEpAIBAAKCAQEAx6vLnpDlqZN7cFHnP7zuh/CEtbAU9/+lq9byNfmi9J3gcXRK\n" +
			"TRMZFWRtBIFsBHf/GZPbNZDBRdK1QgsG6s0KxTlvMM5fZzPkjIJ6Mmue5hOFn6yR\n" +
			"Y2derI1Q7rOBuF1CVgxkNZmvTDIESh2d2jysnD74JEviGI6yPMFjI7f0UofwdVnw\n" +
			"ULSCDGDT2AyNNaDJG6aY2jHUjubydSRxJawLn0O3b6fuBHCA4hwiVoyTs0h28edW\n" +
			"cmfMlV60elVH2eqml1mSL/97JvkE2TQpfgENNf3YuqG7FfzQtdPv8j2PJmhHg8tp\n" +
			"qhMWaf8VUnrv/u9983x4maIr0UYkU5ehtHXIFwIDAQABAoIBADgaDxY4hC8HX1GJ\n" +
			"0b3Y6fSMId2i4eFklToZJOkBgUXV7jAyioXpbczS8MapTp5a0PRw1NDSIVvq/j7W\n" +
			"NlVW00XjA1jlQ20N6ZOJPahZEMZ4ibz5kcgqTTGSa0EGctZIbdEbnjRpQBXan9Xr\n" +
			"SGT/U0h7cOFR6O8ZeP8VHzP0BdQCGge4u/CkWVUNBtCoE30o8iNtNZjSHNmYDW7F\n" +
			"1h510+cA17zjjSC2VXz8i5IplwtdtDdU2Fq8we+ZnLqWB0eW2lrnfhz/oM0R/cRz\n" +
			"w8WpvyLyF7dbbqyshBE8XAwbsfVaRs/Rb80K08cpJgtoNwa9mq4TVnuRUOSZTzWk\n" +
			"gydi/BECgYEA///xTHsaHvlHTPLyWyVOac2yPis53EhvUuE6i5mywjdr7kR7jaQJ\n" +
			"xznqI9weuwcAiDYlZSQP5cGjLLcCoXs0ciPKAxCQ6yQvsPg1yNgT0CiXAwixRa5c\n" +
			"gBBgpw1KFmGN5z17GlhdMy0foSUIkOIwwZLDddn5bH+RfC9yRH3jCUMCgYEAx6vX\n" +
			"Fft4KNTlJPFL2mfKP8S/CbcQW/ziBL+a+U/cOb1qh6n5RVGWtrgZaCCMIxDQLrna\n" +
			"IyNlp98qqXzTqqhBAgRCf1m+D0OsELItOcjpeUSpKwk7Fiviev8konKQrg5zDPhD\n" +
			"Bj284hH0/GLbPuagvgzyObv2wvZ5kvDLSvl73p0CgYEA1aoeELJyQ8XVD9F6HD7P\n" +
			"6pjnGuJjIYtHdwpLHfcDbvxo1e5MnRR7oM+ir5lqALnAZO+kWH0305DBi3GX/YVi\n" +
			"aZ2bXuF1wvxp3a/c3CwUpVkraTnZE4qK48xHj0YVPwbfNFBpLbzw1OTlkE7jjmFr\n" +
			"RjUjdHqgpkD3AYpsOeREGRcCgYAxKI54vAO2Ucvkj3+0Us9Jad/6vVZKv3TJ5H58\n" +
			"GPxIPukmY2OafF7Vt3eR2kmGLJKEnn8lHYbekKQJPFFEd84hd/kDL44KZgfLyII3\n" +
			"8s4AbDYWAVzJtZ451+0mDXyvTaFntQuTUsnZAsDFDnWaA9/ZSoisTCmRkGsYES6L\n" +
			"0+Z6RQKBgQC/U8mDji1noAIojS/Mh+vluWyptcbvN1Mky9dew8YWq5d9jDrSFzAC\n" +
			"XrADa6kGfoLFyUkNhlsRRjW6AAtwuIPrvQJj/FJ/KPCpFUpXbqTaFHxyHZ/J/wPZ\n" +
			"hD3wOBBclthfrUIfgyz0GbD11Xims3PVxKZlzv0auIfdFLaV5gQ8UQ==\n" +
			"-----END RSA PRIVATE KEY-----";
	
	public static final String RSA_PUBLIC="-----BEGIN PUBLIC KEY-----\n" +
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx6vLnpDlqZN7cFHnP7zu\n" +
			"h/CEtbAU9/+lq9byNfmi9J3gcXRKTRMZFWRtBIFsBHf/GZPbNZDBRdK1QgsG6s0K\n" +
			"xTlvMM5fZzPkjIJ6Mmue5hOFn6yRY2derI1Q7rOBuF1CVgxkNZmvTDIESh2d2jys\n" +
			"nD74JEviGI6yPMFjI7f0UofwdVnwULSCDGDT2AyNNaDJG6aY2jHUjubydSRxJawL\n" +
			"n0O3b6fuBHCA4hwiVoyTs0h28edWcmfMlV60elVH2eqml1mSL/97JvkE2TQpfgEN\n" +
			"Nf3YuqG7FfzQtdPv8j2PJmhHg8tpqhMWaf8VUnrv/u9983x4maIr0UYkU5ehtHXI\n" +
			"FwIDAQAB\n" +
			"-----END PUBLIC KEY-----";
}
