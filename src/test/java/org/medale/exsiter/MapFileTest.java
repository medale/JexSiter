package org.medale.exsiter;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.MapFile.Writer.Option;
import org.apache.hadoop.io.Text;
import org.junit.Ignore;

/**
 * Interesting academic exercise but overkill for our use right now - use
 * opencsv instead.
 * 
 */
@Ignore
public class MapFileTest {

	private static final String[] DATA = { "One, two, buckle my shoe",
			"Three, four, shut the door", "Five, six, pick up sticks",
			"Seven, eight, lay them straight", "Nine, ten, a big fat hen" };

	// @Test
	public void testMapFileApi() throws IOException {
		String tmpDirLocation = System.getProperty("java.io.tmpdir");
		String mapDirLocation = tmpDirLocation + File.separator + "map";

		File mapDir = new File(mapDirLocation);
		if (mapDir.exists()) {
			mapDir.delete();
		}

		Configuration conf = new Configuration();
		Path dirPath = new Path(mapDirLocation);

		IntWritable key = new IntWritable();
		Text value = new Text();
		MapFile.Writer writer = null;
		try {
			Option keyClassOption = MapFile.Writer.keyClass(IntWritable.class);
			org.apache.hadoop.io.SequenceFile.Writer.Option valueClassOption = MapFile.Writer
					.valueClass(Text.class);
			writer = new MapFile.Writer(conf, dirPath, keyClassOption,
					valueClassOption);
			for (int i = 0; i < 1024; i++) {
				key.set(i + 1);
				value.set(DATA[i % DATA.length]);
				writer.append(key, value);
			}
		} finally {
			IOUtils.closeStream(writer);
		}
	}
}
