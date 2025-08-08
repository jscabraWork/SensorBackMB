package com.arquitectura;

public class JwtConfig {

	
	public static final String RSA_PRIVATE="-----BEGIN RSA PRIVATE KEY-----\r\n"
			+ "MIIEowIBAAKCAQEAtq62r4jJ+5Emkn6IM9hZCEm+SeB5qVoBwWbY35gBiwwQUQAQ\r\n"
			+ "OTV9q5KTugtJI/6o//fQV352e1aqy2TFiTJockcK3zGWr42b540cQ1QyJ/Ef673i\r\n"
			+ "HFPuI7XamyRk8wJK6iWcCitflX7MbvDEbrTtTia90r/hWGu7Ryt0rmdgmcizOlsm\r\n"
			+ "+7IvjuRYI8lCYyjkOlXnZ92d4hSI3bpA+LpUU3Fzrc6fw5Jaklnvt7eIT+BCvCtc\r\n"
			+ "UcLYCk2cb+fUYqtFGaMpvgo0466JPRVR6KjkTzyHt2rFbkeRvbbbnklvaeiZZPQd\r\n"
			+ "BtWewO3lXHe1fkDU4MaS6lkfnDc32wWuQYqt8QIDAQABAoIBAGLmQ1DUpUTkWBWH\r\n"
			+ "GwW2Yrzx72rfc/4TFFA39cFW8b7tUQgJGL9gDiEMwZ0+7uHlxGiPKOO1Oz+as+xZ\r\n"
			+ "KfgUqibz/xbGKeXwrC63Xk21D1JAYB+oNPlRFuOOWEMVePF2lEgfBFMdGCo+8z48\r\n"
			+ "apvkp3Rf1PR7nOvZpxHonMqufMXDzxAMLieqhx2DxTo7gSWO0u5fftiX/IZ4RwIp\r\n"
			+ "GomTns74FM3rU9+ZVuZcGvP6+5WUsXPsrzEKxGSHuAjprcqAbLosk4Khf2hC/bSI\r\n"
			+ "CZ9K63Hrv8NAxB4JjsfwG7i+A10MudpCZIvzaxlR2WjokQtNjhGs+4srIuFoLycq\r\n"
			+ "nJOYwOECgYEA6t+T6p/NgSnhkJvhSdNyfty0pqw2wqZtFmTWQuSsbUA+tkKdid/Z\r\n"
			+ "oKvemA+SMa+NEmdXh36nfmR6NeYSnI/TceeQwjNIZg8xlKcs9KxGc6fJhP9dTrcA\r\n"
			+ "y+EDLYMzb1/o9xiIKF4cHjRQ1KqkbUyk8oGerA2RJsXqdNeGIDvYMoMCgYEAxx1W\r\n"
			+ "hDInpfd9el6+nfyiprpfaU+4BVLwfAlenocqpU9pM/ARf48BPJYq+6VyodW3Zv2B\r\n"
			+ "q2VrbhYuoIuNXNn0sR7XQWSfvKJ7pHhP7pYcSYp6cdefgbEd7Z9LAKSgY7Q42FWR\r\n"
			+ "qD8I4K5YeJNVX/KV/JVV3n3WPlwxvlfCt4mro3sCgYEAxf3/JRPdGvsBKPQesCtN\r\n"
			+ "I2BN8QrPhrrgzrXmYLGAIQC9XMs/HlDTljwMAsdth/DizRbMlG3SKBvvYaw3GD+u\r\n"
			+ "ESERyIOOjmpUc2mR3heztB9HI5RZRdfDFhryoNUu8L47FsFZVh4vOc8ELKJExdaM\r\n"
			+ "XdmitoshRzwj7qcYa+fG0p0CgYAlfQWUACls2wr55iq3biBlicN6/XJlNjdm8xCo\r\n"
			+ "gkRqOWhSSgnRXWhbMt7G4GCTzbwpHOO0FHVEldKTkZK8/6BNVMQsYLieeG0VCz3p\r\n"
			+ "asPKqNLi6jjy3xkGay5g4Z39B6qIOJwi9DNEsi6gDQVO+kp48tjm+gRDW3JWJwnX\r\n"
			+ "KolxTwKBgH7p6qHalJ3Cs5FzdXLC/OmEiBY575vy0/cEeSe7xFNtdDNAZk5Kg4SW\r\n"
			+ "vbGFF/3MX2o6ZFab75udcPtNGAd685HiSfUlNnkHYlgvrVi+1lg2pmaEBGCpERB/\r\n"
			+ "N2HTFMvXCPRGCXQmP/bBtZJMiJNW19rtMkm+3s3QKcb+ro0PPTCW\r\n"
			+ "-----END RSA PRIVATE KEY-----";
	
	public static final String RSA_PUBLIC="-----BEGIN PUBLIC KEY-----\r\n"
			+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtq62r4jJ+5Emkn6IM9hZ\r\n"
			+ "CEm+SeB5qVoBwWbY35gBiwwQUQAQOTV9q5KTugtJI/6o//fQV352e1aqy2TFiTJo\r\n"
			+ "ckcK3zGWr42b540cQ1QyJ/Ef673iHFPuI7XamyRk8wJK6iWcCitflX7MbvDEbrTt\r\n"
			+ "Tia90r/hWGu7Ryt0rmdgmcizOlsm+7IvjuRYI8lCYyjkOlXnZ92d4hSI3bpA+LpU\r\n"
			+ "U3Fzrc6fw5Jaklnvt7eIT+BCvCtcUcLYCk2cb+fUYqtFGaMpvgo0466JPRVR6Kjk\r\n"
			+ "TzyHt2rFbkeRvbbbnklvaeiZZPQdBtWewO3lXHe1fkDU4MaS6lkfnDc32wWuQYqt\r\n"
			+ "8QIDAQAB\r\n"
			+ "-----END PUBLIC KEY-----";
}
