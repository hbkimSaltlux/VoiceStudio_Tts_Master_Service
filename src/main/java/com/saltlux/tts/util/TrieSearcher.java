package com.saltlux.tts.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieSearcher {

	public class TrieNode {
		
		public int value = -1;
		private final Map<Character, TrieNode> keys = new HashMap<Character, TrieNode>();
		
		public TrieNode get(Character str) {
			return keys.get(str);
		}

		public boolean contain(Character str) {
			return (keys.containsKey(str));
		}
		
		public TrieNode put(Character str, int value) {
			TrieNode tnode = new TrieNode(value);
			keys.put(str, tnode);
			return tnode;
		}

		public TrieNode(int value) {
			this.value = value;
		}
		
		public TrieNode() {
			this(-1);
		}
	}
	
	public class Result {
		public int sp;
		public int ep;
		private int code = -1;
		public String value;
		
		public void set(int code, int sp, int ep) {
			this.code = code;
			this.sp = sp;
			this.ep = ep;
		}
		
		public String toString() {
			return code + ";" + sp + "~" + ep + ";" + value;
		}
	}
	
	private TrieNode root;
	private int count = 0;
	//value에 대한 사전파일
	private Map<Integer, String> ValueMap;
	private Map<String, Integer> ValueDic;
	
	public void clone(TrieSearcher matcher) {
		this.root = matcher.root;
		this.ValueDic = matcher.ValueDic;
		this.ValueMap = matcher.ValueMap;
		this.count = matcher.count;
	}
	
	public static TrieSearcher create(TrieSearcher matcher) {
		TrieSearcher searcher = new TrieSearcher();
		searcher.clone(matcher);
		return searcher;
	}
	
	public String toString() {
		return count + "\t" + root;
	}
	
	public TrieSearcher() {
		root = new TrieNode();
		ValueMap = new HashMap<Integer, String>();
		ValueDic = new HashMap<String, Integer>();
	}
	
	public int size() {
		return count;
	}
	
	private int getDicOffset(String Value) {
		Integer off = ValueDic.get(Value);
		if(off == null) {
			off = ValueDic.size();
			ValueDic.put(Value, off);
			ValueMap.put(off, Value);
		}
		return off;
	}
	public void insq(char[] buf, int offset, int length, String Value) {
		count++;
		TrieNode cnode = root;
		char ch;
		int mx = offset+length;
		for (int i = offset; i < mx; i++) {
			ch = buf[i];
			if (ch <= 'Z' && ch >= 'A') ch = (char) (ch + 32);
			if (!cnode.contain(ch)) {
				int off = -1;
				if(i + 1 == mx) {
					off = getDicOffset(Value);
				}
				cnode = cnode.put(ch, off);
			}
			else {
				cnode = cnode.get(ch);
				if((cnode.value == -1) && (i+1==mx)) {//이미 이전에 결정된 것이 있으면 무시
					int off = getDicOffset(Value);
					cnode.value = off;
				}
			}
		}
	}
	
	public void insq(String Key, String Value) {
		char[] buf = Key.toCharArray();
		insq(buf, 0, buf.length, Value);
	}
	
	public List<Result> search(char[] buf, int offset, int length) {
		List<Result> result = new ArrayList<Result>();
		TrieNode cnode;
		char ch;
		Result tr = new Result();
		int mx = offset+length;
		for (int i = offset; i < mx; i++) {
			cnode = root;
			for (int j = i; j < mx; j++) {
				ch = buf[j];
				if (ch <= 'Z' && ch >= 'A') ch = (char) (ch + 32);
				cnode = cnode.get(ch);
				if(cnode != null) {
					if(cnode.value != -1) {
						tr.set(cnode.value, i, j);
					}
				}
				else {
					if(tr.code != -1) {
						result.add(tr);
						i = tr.ep;
						tr = new Result();
//						i = j-1;
					}
					break;
				}
			}
			if(tr.code != -1) {
				i = tr.ep;
				result.add(tr);
				tr = new Result();
				
			}
		}
		if(tr.code != -1) result.add(tr);
		
		for(Result r : result) r.value = ValueMap.get(r.code);
		
		return result;
	}
	
	public List<Result> search(String instr) {
		char[] chs = instr.toCharArray();
		return search(chs, 0, chs.length);
	}
	
	//JUST TEST
	public static void main(String[] args) throws Exception {
		//송금:TERM 국가:TERM 가:JS 러시아:TERM ,:SW 통화코드:TERM RUB:TERM (:SW 루블:TERM ):SW 로:JS 당발송금:TERM 시:CASE 입력:TERM 는:JS 방법:CMD 
		String instr= "송금 국가가 러시아, 통화코드 RUB(루블)로 당발송금시 입력하는 방법";
		TrieSearcher searcher = new TrieSearcher();
		searcher.insq("당발송금", "term");
		searcher.insq("시", "js");
		searcher.insq("는", "js");
		searcher.insq("입력", "term");
		searcher.insq("하", "js");
		searcher.insq("하는", "js");
		searcher.insq("하는 방법", "cmd");
		List<Result> result = searcher.search(instr);

		for(int i = 0 ; i < result.size(); i++) {
			Result r = result.get(i);
			System.out.println(instr.substring(r.sp, r.ep+1) + "\t" + r.value);
		}
	}
}

