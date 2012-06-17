package com.github.filipesperandio.vraptor.hypermedia;

import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.serialization.xstream.Serializee;
import br.com.caelum.vraptor.serialization.xstream.VRaptorClassMapper;

import com.google.common.base.Supplier;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * TEMP SOLUTION
 * 
 * @author filipesperandio
 *
 */
public class HypermediaXStream extends XStream {
	private final TypeNameExtractor extractor;
	private VRaptorClassMapper vraptorMapper;

	{
		setMode(NO_REFERENCES);
	}

	public HypermediaXStream(TypeNameExtractor extractor) {
		super();
		this.extractor = extractor;
	}

	public HypermediaXStream(TypeNameExtractor extractor,
			HierarchicalStreamDriver hierarchicalStreamDriver) {
		super(hierarchicalStreamDriver);
		this.extractor = extractor;
	}

	@Override
	protected MapperWrapper wrapMapper(MapperWrapper next) {

		vraptorMapper = new VRaptorClassMapper(next,
		new Supplier<TypeNameExtractor>() {
			public TypeNameExtractor get() {
				return extractor;
			}
		});
		vraptorMapper.setSerializee(new Serializee());
		return vraptorMapper;
	}

	public VRaptorClassMapper getVRaptorMapper() {
		return vraptorMapper;
	}
}
