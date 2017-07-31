package jp.ac.keio.med.rad.numpy;
/*Copyright (c) 2017, m.hashimoto@rad.med.keio.ac.jp
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
* Neither the name of the <organization> nor the names of its contributors
  may be used to endorse or promote products derived from this software
  without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Numpy {

	public static final byte[] MAGIC_STRING = {(byte)0x93,(byte)0x4E,(byte)0x55,(byte)0x4D,(byte)0x50,(byte)0x59};
	public static final byte[] VERSION = {(byte)0x01,(byte)0x00};

	public static void save(float[][] array, Path path) throws IOException{
		int row = array.length;
		int column = array[0].length;
		int[] shape = {row, column};
		float[] flatArray = new float[row * column];
		for(int i = 0; i < row; i++){
			System.arraycopy(array[i], 0, flatArray, column * i, column);
		}
		save(shape, flatArray, path);
	}

	public static void save(double[][] array, Path path) throws IOException{
		int row = array.length;
		int column = array[0].length;
		int[] shape = {row, column};
		double[] flatArray = new double[row * column];
		for(int i = 0; i < row; i++){
			System.arraycopy(array[i], 0, flatArray, column * i, column);
		}
		save(shape, flatArray, path);
	}

	public static void save(int[][] array, Path path) throws IOException{
		int row = array.length;
		int column = array[0].length;
		int[] shape = {row, column};
		int[] flatArray = new int[row * column];
		for(int i = 0; i < row; i++){
			System.arraycopy(array[i], 0, flatArray, column * i, column);
		}
		save(shape, flatArray, path);
	}

	public static void save(int[] shape, float[] array, Path path) throws IOException{
		ByteBuffer buffer = header(shape, array.length, "<f4", 4);
		for(float f: array){
			buffer.putFloat(f);
		}
		buffer.flip();
		save(buffer, path);
	}

	public static void save(int[] shape, double[] array, Path path) throws IOException{
		ByteBuffer buffer = header(shape, array.length, "<f8", 8);
		for(double f: array){
			buffer.putDouble(f);
		}
		buffer.flip();
		save(buffer, path);
	}

	public static void save(int[] shape, int[] array, Path path) throws IOException{
		ByteBuffer buffer = header(shape, array.length, "<i4", 4);
		for(int f: array){
			buffer.putInt(f);
		}
		buffer.flip();
		save(buffer, path);
	}

	private static ByteBuffer header(int[] shape, int arrayLength, String dtype, int valueLength){
		int dimension = shape.length;
		int product = 1;
		for(int i = 0; i < dimension; i++){
			if(shape[i] <= 0){
				throw new IllegalArgumentException("Shape should > 0 . shape[ " + i + "]:" + shape[i]);
			}
			product *= shape[i];
		}
		if(product != arrayLength){
			throw new IllegalArgumentException("Shape do not match array length : " + arrayLength);
		}
		StringBuilder sb = new StringBuilder("{'descr': '");
		sb.append(dtype);
		sb.append("', 'fortran_order': False, 'shape': (");
		if(1 < dimension){
			for(int d = 0; d < dimension; d++){
				sb.append(shape[d]);
				if(d + 1 != dimension){
					sb.append(", ");
				}
			}
		}else{
			sb.append(shape[0] + ",");
		}
		sb.append("), }");
		String dic = sb.toString();
		int paddingLen = 16 - (10 + dic.length() + 1) % 16;
		int headerLen = dic.length() + paddingLen + 1;
		ByteBuffer buffer = ByteBuffer.allocate(11 + headerLen + arrayLength * valueLength).order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(MAGIC_STRING);
		buffer.put(VERSION);
		buffer.putShort((short)headerLen);
		buffer.put(dic.getBytes(StandardCharsets.US_ASCII));
		for(int i = 0; i < paddingLen; i++){
			buffer.put((byte)0x20);
		}
		buffer.put((byte)0x0a);
		return buffer;
	}

	private static void save(ByteBuffer buffer, Path path) throws IOException{
		FileChannel fileChannel = FileChannel.open(path,StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		fileChannel.write(buffer);
		fileChannel.close();
	}

}