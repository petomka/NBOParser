package nbo;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

	public record Vector3f (float x, float y, float z) {
		public static Vector3f deserialize(Map<String, ?> map) {
			return new Vector3f(
					(float) map.get("x"),
					(float) map.get("y"),
					(float) map.get("z")
			);
		}

		public Map<String, Object> serialize() {
			return Map.of(
					"x", x,
					"y", y,
					"z", z
			);
		}
	}

	public record Matrix3f (Vector3f col1, Vector3f col2, Vector3f col3) {
		public static Matrix3f deserialize(Map<String, ?> map) {
			return new Matrix3f(
					(Vector3f) map.get("col1"),
					(Vector3f) map.get("col2"),
					(Vector3f) map.get("col3")
			);
		}

		public Map<String, Object> serialize() {
			return Map.of(
					"col1", col1,
					"col2", col2,
					"col3", col3
			);
		}
	}

	private final String example1 = "<with Tree as OtherTree> assign := Type{key: '&reference', list: ['a', 'x', 'v']}";
	private final String example2 = "<with Tree as OtherTree>\n<with Cow as Pig>\nxy := Type{key: 'a'}  assign := Type{key: &xy, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list2: [1b, 0B, true], list: ['x', {key: 'value'}, 'z', 'a', 'b']}";

	private static final File TEST_FILE = new File("src/main/resources/test.nbo");
	private static final File TEST_FILE_VECTOR = new File("src/main/resources/vector_test.nbo");

	@Test
	void tokenize() throws NBOParseException {
		new NBOParser().getTokenizer().tokenize("assign := Type{float: 1.0f, int: 0x1f0, bool: 1b}").forEach(t -> System.out.print(t.string() + "(" + t.token() + ") <-> "));
	}

	@Test
	void parse() throws NBOParseException {
		NBOParser parser = new NBOParser();
		parser.tokenize(example1);
		System.out.println(new NBOPrettyPrinter().format(parser.createAST()));
	}

	@Test
	public void printStringAsFile() throws NBOParseException {
		NBOParser parser = new NBOParser();
		parser.tokenize(example2);
		System.out.println(NBOFile.formatToFileString(parser.createAST()));
	}

	@Test
	public void writeToFile() throws IOException, NBOParseException, ClassNotFoundException {
		NBOSerializer.register(File.class, s -> new File((String) s.get("path")), f -> Map.of("path", f.getAbsolutePath()));
		NBOFile file = NBOFile.loadFile(TEST_FILE);
		file.setObject("a_file", TEST_FILE);
		System.out.println(NBOFile.formatToFileString(file.getRoot()));
	}

	@Test
	public void readFromFile() throws IOException, NBOParseException, ClassNotFoundException {
		NBOSerializer.register(
				Vector3f.class,
				Vector3f::deserialize,
				Vector3f::serialize
		);
		NBOSerializer.register(
				Matrix3f.class,
				Matrix3f::deserialize,
				Matrix3f::serialize
		);
		NBOFile file = NBOFile.loadFile(TEST_FILE_VECTOR);

		Vector3f unit1 = file.get("unit_1");
		assertNotNull(unit1, "unit1 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(1.0f, unit1.x);
		assertEquals(0.0f, unit1.y);
		assertEquals(0.0f, unit1.z);

		Vector3f unit2 = file.get("unit_2");
		assertNotNull(unit2, "unit2 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(0.0f, unit2.x);
		assertEquals(1.0f, unit2.y);
		assertEquals(0.0f, unit2.z);

		Vector3f unit3 = file.get("unit_3");
		assertNotNull(unit3, "unit3 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(0.0f, unit3.x);
		assertEquals(0.0f, unit3.y);
		assertEquals(1.0f, unit3.z);

		Matrix3f matrix3f = file.get("mat");
		assertNotNull(matrix3f, "matrix3f should not be null, as it should be defined in vector_test.nbo");
		assertEquals(unit1, matrix3f.col1);
		assertEquals(unit2, matrix3f.col2);
		assertEquals(unit3, matrix3f.col3);
	}

}